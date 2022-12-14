package com.stock.notification.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.StockDao;
import com.stock.notification.dao.StockTradingDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.entity.UserAlertEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import com.stock.notification.vo.StockVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("stockTradingService")
@Slf4j
public class StockTradingServiceImpl extends ServiceImpl<StockTradingDao, StocktradingEntity> implements StockTradingService {

    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * @CacheEvict:失效模式
     *      * @CachePut:双写模式，需要有返回值
     *      * 1、同时进行多种缓存操作：@Caching
     *      * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "stockTrading",allEntries = true)
     *      * 3、存储同一类型的数据，都可以指定为同一分区
     * @param stocktradingEntity
     */
    @CacheEvict(value = "stockTrading",allEntries = true)       //删除某个分区下的所有数据
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStockTrading(StocktradingEntity stocktradingEntity) {

        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("StockTradingJSON-lock");
        //创建写锁
        RLock rLock = readWriteLock.writeLock();
        try {
            rLock.lock();
            this.baseMapper.updateById(stocktradingEntity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

    }



    @Override
    public Map<String, StocktradingEntity> getStockTradingJson(String stockcode) {
        // 给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型（序列化与反序列化）
        /**
         * 1 空结果缓存，解决缓存穿透
         * 2 设置过期时间（加随机值） ，解决缓存雪崩
         * 3 加锁，解决缓存击穿
         */
        // 1 加入缓存逻辑，缓存中存的数据是json字符串
        // JSON跨语言，跨平台兼容
        String stockTradingJSON = redisTemplate.opsForValue().get("stockTradingJSON");
        if (!StringUtils.hasLength(stockTradingJSON)) {
            // 2 缓存中没有，查询数据库
            log.info("缓存不命中...将要查询数据库");
            Map<String, StocktradingEntity> stockTradingFromDb = getStockTradingJsonWithRedislock(stockcode);
            return stockTradingFromDb;
        }
        log.info("缓存命中...直接返回");
        Map<String, StocktradingEntity> result = JSON.parseObject(stockTradingJSON, new TypeReference<Map<String, StocktradingEntity>>() {
        });

        return result;
    }


    /**
     * 加分布式锁
     * @return
     */
    public Map<String, StocktradingEntity> getStockTradingJsonWithRedislock(String stockcode) {

        //1、占分布式锁。去redis占坑
        //（锁的粒度，越细越快:具体缓存的是某个数据）
        //创建读锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("StockTradingJSON-lock");
        RLock rLock = readWriteLock.readLock();
        Map<String, StocktradingEntity> dataFromDB=null;
        try {
            rLock.lock();
            //从数据库中访问数据
            dataFromDB = getDataFromDB(stockcode);

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
    private Map<String, StocktradingEntity> getDataFromDB(String stockcode) {
        String stockTradingJSON = redisTemplate.opsForValue().get("stockTradingJSON");
        if (!StringUtils.hasLength(stockTradingJSON)) {
            // 缓存不为null直接返回
            Map<String, StocktradingEntity> result = JSON.parseObject(stockTradingJSON, new TypeReference<Map<String, StocktradingEntity>>() {
            });
            return result;
        }
       log.info("查询了数据库......");

        StocktradingEntity stocktradingEntity =baseMapper.selectOne(new QueryWrapper<StocktradingEntity>().eq("stockcode",stockcode));
        log.info(String.valueOf(stocktradingEntity));

        Map<String, StocktradingEntity> stockTradingMap =null;
        stockTradingMap.put(stockcode,stocktradingEntity);

        // 3 查到的数据放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(stockTradingMap);
        redisTemplate.opsForValue().set("stockTradingJSON", s, 1, TimeUnit.DAYS);
        return stockTradingMap;
    }

    /**
     * 获取股票代码，并存为list
     * @param selectList
     * @param stock_code
     * @return
     */
    private List<StocktradingEntity> getStock_code(List<StocktradingEntity> selectList, String stock_code) {
        List<StocktradingEntity> collect = selectList.stream().filter(item -> {
            return item.getStockCode() == stock_code;
        }).collect(Collectors.toList());
        return collect;
        // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("stock_code", v.getCatId()));
    }


    /**
     * 监控股票变动信息
     * @param stockCode
     */
    @Override
    public StockVo monitorStockChange(String stockCode){
        StockVo stockVo = new StockVo();
        Map<String, StocktradingEntity> concurrntStock = getStockTradingJson(stockCode);
         StocktradingEntity stocktradingEntity = concurrntStock.get(stockCode);
         //最新价格
         BigDecimal latestPrice= stocktradingEntity.getLatestPrice();
         //昨收
         BigDecimal pre=stocktradingEntity.getPre();
         //涨跌幅
         BigDecimal changePercent=pre.divide(latestPrice.subtract(pre),3,BigDecimal.ROUND_HALF_UP);
         stockVo.setLatestPrice(latestPrice);
         stockVo.setStockCode(stockCode);
         stockVo.setStockChange(changePercent);
         stockVo.setPre(pre);
         if (changePercent.compareTo(new BigDecimal(0))>0){
             rabbitTemplate.convertAndSend("stock-event-exchange","stock.pricerise",stockVo);
             rabbitTemplate.convertAndSend("stock-event-exchange","stock.priceriseover",stockVo);
         }else if (changePercent.compareTo(new BigDecimal(0))<0){
             rabbitTemplate.convertAndSend("stock-event-exchange","stock.pricefall",stockVo);
             rabbitTemplate.convertAndSend("stock-event-exchange","stock.pricefallover",stockVo);
         }
         return stockVo;
    }
}
