package com.stock.notification.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.notification.entity.StockEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockDao extends BaseMapper<StockEntity> {
//    List<StockEntity> findAllStock();

}
