package com.stock.notification.service;

import com.stock.notification.entity.StocktradingEntity;

import java.util.List;
import java.util.Map;

public interface StockTradingService {
    Map<String, StocktradingEntity> getStockTradingJson(String stockcode);
}
