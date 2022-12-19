package com.stock.notification.service;

import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.UserAlertEntity;
import com.stock.notification.vo.StockVo;
import com.stock.notification.vo.UserAlertVo;

import java.math.BigDecimal;

public interface UserAlertService {
    void addAlert(int userId, String stockCode, int alertType, BigDecimal alertContent, int alertFrequency);

    UserAlertVo notifyUser(StockVo stockVo);

//    UserAlertEntity getUserAlertInfo(int userId, String stockCode, int alertType, BigDecimal alertContent, int alertFrequency);

//    UserAlertVo notifyFall(StockVo stockVo);
//
//    UserAlertVo notifyRise(StockVo stockVo);
//
//    UserAlertVo notifyRiseOver(StockVo stockVo);

}

