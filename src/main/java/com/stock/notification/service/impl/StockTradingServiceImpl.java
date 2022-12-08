package com.stock.notification.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.StockDao;
import com.stock.notification.dao.StockTradingDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("stockTradingService")
@Slf4j
public class StockTradingServiceImpl extends ServiceImpl<StockTradingDao, StocktradingEntity> implements StockTradingService {

    @Autowired
    RedissonClient redisson;

    @Resource
    private StringRedisTemplate redisTemplate;


    /**
     * 从缓存中取
     * @return
     */
    @Override
    public Map<String, List<StocktradingEntity>> getStockTradingJson() {

        // 采用Redisson分布式锁
        RLock lock = redisson.getLock("StockTrading-lock");
        lock.lock();
        Map<String, List<StocktradingEntity>> dataFromDB;
        try {
            //从数据库中访问数据
            dataFromDB = getDataFromDB();

        } finally {
            lock.unlock();
        }
        return dataFromDB;

    }

    /**
     * 从数据库中取
     * @return
     */
    private Map<String, List<StocktradingEntity>> getDataFromDB() {
        String stockTradingJSON = redisTemplate.opsForValue().get("stockTradingJSON");
        if (!StringUtils.isEmpty(stockTradingJSON)) {
            // 缓存不为null直接返回
            Map<String, List<StocktradingEntity>> result = JSON.parseObject(stockTradingJSON, new TypeReference<Map<String, List<StocktradingEntity>>>() {
            });
            return result;
        }
       log.info("查询了数据库......");

        List<StocktradingEntity> selectList =baseMapper.selectList(null);
        log.info(String.valueOf(selectList));
        // 查询所有股票代码
        List<StocktradingEntity> level1stock = getStock_code(selectList, null);

        // 2 封装数据
        Map<String, List<StocktradingEntity>> stockTradingMap = level1stock.stream().collect(Collectors.toMap(k -> k.getStockCode().toString(), v -> {
                    // 1 每一个的一级分类，查到这个一级分类的二级分类
                    return selectList;
                }

        ));

        // 3 查到的数据放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(stockTradingMap);
        redisTemplate.opsForValue().set("stockTradingJSON", s, 1, TimeUnit.DAYS);
        return stockTradingMap;
    }

    private List<StocktradingEntity> getStock_code(List<StocktradingEntity> selectList, String stock_code) {
        List<StocktradingEntity> collect = selectList.stream().filter(item -> {
            return item.getStockCode() == stock_code;
        }).collect(Collectors.toList());
        return collect;
        // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("stock_code", v.getCatId()));
    }
}
