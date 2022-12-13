package com.stock.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.StockDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import com.stock.notification.service.UserStockRelationService;
import com.stock.notification.vo.StockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service("stockService")
public class StockServiceImpl extends ServiceImpl<StockDao, StockEntity> implements StockService {



    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private UserStockRelationService userStockRelationService;


    @Override
    @Cacheable(value = "stockCache",key = "#stock")
    public Map<String,List<StockEntity>> queryStock(int userId) {
        //推荐股
        Map<String,List<StockEntity>> map=null;

        StockVo stockVo = new StockVo();
        //1、获取推荐股票 stockinfo
        CompletableFuture<Void> recommendFuture = CompletableFuture.runAsync(() -> {
            List<StockEntity> recommendList= this.baseMapper.selectList(new QueryWrapper<StockEntity>());
            map.put("推荐股票",recommendList);
        }, executor);

        //2、获取自选股票 userown
        CompletableFuture<Void> selectFuture = CompletableFuture.runAsync(() -> {
            List<StockEntity> selectList = userStockRelationService.querySelectStock(userId);
            map.put("用户所持股票",selectList);
        }, executor);

        try {
            CompletableFuture.allOf(recommendFuture,selectFuture).get();
        } catch (InterruptedException e) {
            log.info("异步编排出现中断异常："+e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void addShares(String stockCode, int userId) {
        userStockRelationService.addShares(stockCode,userId);
    }

    @Override
    public void removeShares(String stockCode, int userId) {
        userStockRelationService.removeShares(stockCode,userId);
    }

    @Override
    public void collectShares(String stockCode, int userId) {
        userStockRelationService.addCollect(stockCode,userId);
    }

    @Override
    public StockEntity getByCode(String stockCode) {
        return  baseMapper.selectById(new QueryWrapper<StockEntity>().eq("stockcode",stockCode));
    }


}
