package com.stock.notification.config;

import com.alibaba.fastjson.serializer.GuavaCodec;
import com.google.common.cache.*;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.vo.StockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author:luoyang
 * @email:772767100@qq.com
 * @date 2022-12-7，10：00
 *
 */


@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "guava.cache.config")
@Data
@Slf4j
@Component
public class MyGuavaConfig {

    private int concurrencyLevel;

    private long maximumSize;

    private long expireAfterWrite;

    private long refreshAfterWrite;

    private int initialCapacity;

    @Autowired
    private StockService stockService;

    @Bean
    public void cacheManager() {

//        GuavaCacheManager cacheManager = new GuavaCacheManager();

        log.info("并发级别为：" + concurrencyLevel + "缓存过期时间为：" + expireAfterWrite + "缓存容量为：" + initialCapacity + "缓存最大容量为：" + maximumSize);
        LoadingCache<String, List<StockEntity>> cacheManager = CacheBuilder.newBuilder()

                //设置并发级别为8   并发级别是指可以同事写缓存的线程数
                .concurrencyLevel(concurrencyLevel)
                //设置写缓存后10s过期
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                //设置缓存容量的初始容量为10
                .initialCapacity(initialCapacity)
                //设置缓存最大容量为100,超过100之后会按照LRU最近最少使用算法来移除缓存项
                .maximumSize(maximumSize)
                //设置要统计缓存的命中率
                .recordStats()
                //设置缓存移除通知
                //如果缓存过期，恰好有多个线程读取同一个key的值，那么guava只允许一个线程去加载数据，其余线程阻塞。
                // 这虽然可以防止大量请求穿透缓存，但是效率低下。使用refreshAfterWrite可以做到：只阻塞加载数据的线程，其余线程返回旧数据。
                .refreshAfterWrite(refreshAfterWrite, TimeUnit.MINUTES)
                .removalListener(
                        new RemovalListener<Object, Object>() {
                            @Override
                            public void onRemoval(RemovalNotification<Object, Object> notification) {
                                log.info(notification.getKey() + " was removed, cause is " + notification.getCause());
                            }
                        }
                ).build(new StockLoader());

    }

    public class StockLoader extends CacheLoader<String,List<StockEntity>>{
        @Override
        public List<StockEntity> load(String key) {
            log.info("加载股票信息开始");
            List<StockEntity> list=stockService.queryStock();
            log.info("list:{}",list.size());
            log.info("加载股票信息结束");
            return list;
        }

    }
}
