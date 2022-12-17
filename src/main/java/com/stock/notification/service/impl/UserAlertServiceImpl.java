package com.stock.notification.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.UserAlertDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.entity.UserAlertEntity;
import com.stock.notification.entity.UserEntity;
import com.stock.notification.service.UserAlertService;
import com.stock.notification.vo.StockVo;
import com.stock.notification.vo.UserAlertVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("userAlertService")
@Slf4j
public class UserAlertServiceImpl extends ServiceImpl<UserAlertDao, UserAlertEntity> implements UserAlertService {

    @Resource
    private UserAlertDao userAlertDao;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 添加用户自定义预警入库
     * @param userId
     * @param stockCode
     * @param alertType
     * @param alertContent
     * @param alertFrequency
     */
    @Override
    public void addAlert(int userId, String stockCode, int alertType, BigDecimal alertContent, int alertFrequency) {
        UserAlertEntity userAlertEntity = new UserAlertEntity();
        userAlertEntity.setStockCode(stockCode);
        userAlertEntity.setUserId(userId);
        userAlertEntity.setAlertType(alertType);
        userAlertEntity.setAlertContent(alertContent);
        userAlertEntity.setAlertFrequency(alertFrequency);
        userAlertDao.insert(userAlertEntity);
    }

    @Override
    public UserAlertVo notifyUser(StockVo stockVo) {
        String stockCode=stockVo.getStockCode();
        float latestPrice=stockVo.getLatestPrice().floatValue();

        //获取到该股票的用户预警信息
        UserAlertEntity userAlertEntity=getUserAlertJson(stockCode).get(stockCode);
        int userId=userAlertEntity.getUserId();
        int alertType=userAlertEntity.getAlertType();
        int alertFrequency = userAlertEntity.getAlertFrequency();

        //获取到该股票，该预警类型一致的用户列表
        Set<String> userSet=getUserAlertSet(userId,stockCode,alertType,latestPrice);


        //通知用户信息 数据封装
        UserAlertVo userAlertVo=new UserAlertVo();
        userAlertVo.setUserSet(userSet);
        userAlertVo.setStockCode(stockCode);
        userAlertVo.setLatestPrice(stockVo.getLatestPrice());
        userAlertVo.setStockChange(stockVo.getStockChange());
        userAlertVo.setUserExpect(userAlertEntity.getAlertContent());
        userAlertVo.setAlertFrequency(alertFrequency);
        return userAlertVo;
    }




    /**
     * 用户与期望值存入缓存，并zset score存取预警预期数值
     * @return
     */
    public Set<String> getUserAlertSet(int userId,String stockCode,int alertType,float latestPrice) {
        //查询股票对应用户列表及期望数值
        Set<String> userSet=null;

        //获取股票上涨用户
        if(alertType==1){
            userSet=redisTemplate.opsForZSet().rangeByScore(stockCode,latestPrice,Integer.MAX_VALUE);
        }
        //获取股票下跌用户
        else if (alertType==2){
            userSet=redisTemplate.opsForZSet().rangeByScore(stockCode,0,latestPrice);
        }

        if (userSet.isEmpty()) {
            // 2 缓存中没有，查询数据库
            log.info("缓存不命中...将要查询数据库");
            Set<String> alertJsonFromDb = getAlertSetFromDbWithRedislock(userId,stockCode,alertType,latestPrice);
            return alertJsonFromDb;
        }
        log.info("缓存命中...直接返回");
        return userSet;
    }

    /**
     * 股票与用户预警信息同时也存入缓存
     */
    public Map<String, UserAlertEntity> getUserAlertJson(String stockcode) {
        String stockTradingJSON = redisTemplate.opsForValue().get("userAlertJSON");
        if (!StringUtils.hasLength(stockTradingJSON)) {
            // 2 缓存中没有，查询数据库
            log.info("缓存不命中...将要查询数据库");
            Map<String, UserAlertEntity> stockTradingFromDb = getUserAlertJsonWithRedislock(stockcode);
            return stockTradingFromDb;
        }
        log.info("缓存命中...直接返回");
        Map<String, UserAlertEntity> result = JSON.parseObject(stockTradingJSON, new TypeReference<Map<String, UserAlertEntity>>() {
        });

        return result;
    }


    /**
     * 从缓存中取
     * @return
     */
    public Set<String> getAlertSetFromDbWithRedislock(int userId,String stockCode,int alertType, float latestPrice) {

        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("UserAlertJSON-lock");
        RLock rLock = readWriteLock.readLock();
        Set<String> dataFromDB=null;
        try {
            rLock.lock();
            //从数据库中访问数据
            dataFromDB = getDataFromDB(userId,stockCode,alertType,latestPrice);

        }
        finally {
            rLock.unlock();
        }
        return dataFromDB;

    }

    /**
     * 加分布式锁
     * @return
     */
    public Map<String, UserAlertEntity> getUserAlertJsonWithRedislock(String stockcode) {

        //1、占分布式锁。去redis占坑
        //（锁的粒度，越细越快:具体缓存的是某个数据）
        //创建读锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("UserAlertJSON-lock");
        RLock rLock = readWriteLock.readLock();
        Map<String, UserAlertEntity> dataFromDB=null;
        try {
            rLock.lock();
            //从数据库中访问数据
            dataFromDB = getJSONDataFromDB(stockcode);

        }
        finally {
            rLock.unlock();
        }
        return dataFromDB;

    }

    /**
     * 从数据库中取
     * @return
     */
    private Set<String> getDataFromDB(int userId,String stockCode,int alertType,float latestPrice) {
        Set<String> userSet=null;

        //获取股票上涨用户
        if(alertType==1){
            userSet=redisTemplate.opsForZSet().rangeByScore(stockCode,latestPrice,Integer.MAX_VALUE);
        }
        //获取股票下跌用户
        else if (alertType==2){
            userSet=redisTemplate.opsForZSet().rangeByScore(stockCode,0,latestPrice);
        }


        if (!userSet.isEmpty()) {
            // 缓存不为null直接返回
            return userSet;
        }
        log.info("查询了数据库......");
        UserAlertEntity userAlertEntity =baseMapper.selectById(new QueryWrapper<UserAlertEntity>().eq("userid",userId).eq("stockcode",stockCode).eq("alertType",alertType));
        log.info(String.valueOf(userAlertEntity));
        redisTemplate.opsForZSet().add(stockCode,String.valueOf(userAlertEntity.getUserId()),userAlertEntity.getAlertContent().floatValue());
        return userSet;
    }

    /**
     * 从数据库中取
     * @return
     */
    private Map<String, UserAlertEntity> getJSONDataFromDB(String stockcode) {
        String stockTradingJSON = redisTemplate.opsForValue().get("stockTradingJSON");
        if (!StringUtils.hasLength(stockTradingJSON)) {
            // 缓存不为null直接返回
            Map<String, UserAlertEntity> result = JSON.parseObject(stockTradingJSON, new TypeReference<Map<String, UserAlertEntity>>() {
            });
            return result;
        }
        log.info("查询了数据库......");

        UserAlertEntity userAlertEntity =baseMapper.selectOne(new QueryWrapper<UserAlertEntity>().eq("stockcode",stockcode));
        log.info(String.valueOf(userAlertEntity));

        Map<String, UserAlertEntity> userAlertMap =null;
        userAlertMap.put(stockcode,userAlertEntity);

        // 3 查到的数据放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(userAlertMap);
        redisTemplate.opsForValue().set("userAlertJSON", s, 1, TimeUnit.DAYS);
        return userAlertMap;
    }

    /**
     * 获取股票代码，并存为list
     * @param selectList
     * @param stock_code
     * @return
     */
    private List<UserAlertEntity> getStock_code(List<UserAlertEntity> selectList, String stock_code) {
        List<UserAlertEntity> collect = selectList.stream().filter(item -> {
            return item.getStockCode() == stock_code;
        }).collect(Collectors.toList());
        return collect;
        // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("stock_code", v.getCatId()));
    }

}
