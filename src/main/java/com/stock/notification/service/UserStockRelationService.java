package com.stock.notification.service;

import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.UserEntity;

import java.util.List;

public interface UserStockRelationService {
    List<StockEntity> querySelectStock(int userId);

    void addShares(String stockCode, int userId);

    void removeShares(String stockCode, int userId);

    void addCollect(String stockCode, int userId);

    List<UserEntity> queryUserByStock(String stockCode);

}
