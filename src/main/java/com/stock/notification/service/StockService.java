package com.stock.notification.service;

import com.stock.notification.entity.StockEntity;
import com.stock.notification.vo.StockVo;

import java.util.List;

public interface StockService {
    List<StockEntity> queryStock() ;
}
