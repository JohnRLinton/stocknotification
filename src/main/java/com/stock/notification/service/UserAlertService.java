package com.stock.notification.service;

import com.stock.notification.entity.StockEntity;

public interface UserAlertService {
    void addAlert(int userId, String stockCode, int alertType, String alertContent, int alertFrequency);

    void notifyFallOver(StockEntity stockEntity);

    void notifyFall(StockEntity stockEntity);

    void notifyRise(StockEntity stockEntity);

    void notifyRiseOver(StockEntity stockEntity);

}

