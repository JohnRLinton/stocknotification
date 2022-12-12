package com.stock.notification.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.UserAlertDao;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.entity.UserAlertEntity;
import com.stock.notification.entity.UserStockRelationEntity;
import com.stock.notification.service.UserAlertService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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
    RedissonClient redisson;

    /**
     * 添加用户自定义预警入库
     * @param userId
     * @param stockCode
     * @param alertType
     * @param alertContent
     * @param alertFrequency
     */
    @Override
    public void addAlert(int userId, String stockCode, int alertType, String alertContent, int alertFrequency) {
        UserAlertEntity userAlertEntity = new UserAlertEntity();
        userAlertEntity.setStockCode(stockCode);
        userAlertEntity.setUserId(userId);
        userAlertEntity.setAlertType(alertType);
        userAlertEntity.setAlertContent(alertContent);
        userAlertEntity.setAlertFrequency(alertFrequency);
        userAlertDao.insert(userAlertEntity);
    }


    /**
     * 存入缓存，并zset存取预警
     * @return
     */
    public Map<String, List<UserAlertEntity>> getUserAlertJson(int userId,String stockCode) {
        // 给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型（序列化与反序列化）
        //TODO:zset
        String alertJSON = redisTemplate.opsForValue().get("userAlertJSON");
        if (StringUtils.hasLength(alertJSON)) {
            // 2 缓存中没有，查询数据库
            log.info("缓存不命中...将要查询数据库");
            Map<String, List<UserAlertEntity>> alertJsonFromDb = getAlertJsonFromDbWithRedislock(userId,stockCode);
            return alertJsonFromDb;
        }
        log.info("缓存命中...直接返回");
        Map<String, List<UserAlertEntity>> result = JSON.parseObject(alertJSON, new TypeReference<Map<String, List<UserAlertEntity>>>() {
        });
        return result;
    }


    /**
     * 从缓存中取
     * @return
     */
    public Map<String, List<UserAlertEntity>> getAlertJsonFromDbWithRedislock(int userId,String stockCode) {

        // 采用Redisson分布式锁
        RLock lock = redisson.getLock("userAlert-lock");
        lock.lock();
        Map<String, List<UserAlertEntity>> dataFromDB;
        try {
            //从数据库中访问数据
            dataFromDB = getDataFromDB(userId,stockCode);
        }
        finally {
            lock.unlock();
        }
        return dataFromDB;

    }

    /**
     * 从数据库中取
     * @return
     */
    private Map<String, List<UserAlertEntity>> getDataFromDB(int userId,String stockCode) {
        String userAlertJSON = redisTemplate.opsForValue().get("alertJSON");
        if (!StringUtils.hasLength(userAlertJSON)) {
            // 缓存不为null直接返回
            Map<String, List<UserAlertEntity>> result = JSON.parseObject(userAlertJSON, new TypeReference<Map<String, List<UserAlertEntity>>>() {
            });
            return result;
        }
        log.info("查询了数据库......");

        List<UserAlertEntity> selectList =baseMapper.selectList(new QueryWrapper<UserAlertEntity>().eq("userid",userId).eq("stockcode",stockCode));
        log.info(String.valueOf(selectList));
        // 查询股票代码额外信息
        List<UserAlertEntity> level1stock = getStock_code(selectList, null);

        // 2 封装数据
        Map<String, List<UserAlertEntity>> stockTradingMap = level1stock.stream().collect(Collectors.toMap(k -> k.getStockCode().toString(), v -> {
                    // 1 每一个的一级分类，查到这个一级分类的二级分类
                    return level1stock;
                }

        ));

        // 3 查到的数据放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(stockTradingMap);
        redisTemplate.opsForValue().set("alertJSON", s, 1, TimeUnit.DAYS);
        return stockTradingMap;
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
