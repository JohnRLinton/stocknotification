package com.stock.notification.service;

import com.stock.notification.entity.StockEntity;

import java.math.BigDecimal;

public interface UserAlertService {
    void addAlert(int userId, String stockCode, int alertType, BigDecimal alertContent, int alertFrequency);

    void notifyFallOver(StockEntity stockEntity);

    void notifyFall(StockEntity stockEntity);

    void notifyRise(StockEntity stockEntity);

    void notifyRiseOver(StockEntity stockEntity);

}

