package com.stock.notification.service;

import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.vo.StockVo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface StockTradingService {
    @CacheEvict(value = "category",allEntries = true)       //删除某个分区下的所有数据
    @Transactional(rollbackFor = Exception.class)
    void updateStockTrading(StocktradingEntity stocktradingEntity);

    Map<String, StocktradingEntity> getStockTradingJson(String stockcode);

    StockVo monitorStockChange(String stockCode);
}
