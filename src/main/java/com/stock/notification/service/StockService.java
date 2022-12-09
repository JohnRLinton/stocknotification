package com.stock.notification.service;

import com.stock.notification.entity.StockEntity;
import com.stock.notification.vo.StockVo;

import java.util.List;
import java.util.Map;

public interface StockService {
    Map<String,List<StockEntity>> queryStock(int userId) ;

    void addShares(String stockCode, int userId);

    void removeShares(String stockCode, int userId);

    void collectShares(String stockCode, int userId);

    StockEntity getByCode(String stockCode);

}
