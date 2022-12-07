package com.stock.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.StockDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import com.stock.notification.vo.StockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("stockService")
public class StockServiceImpl extends ServiceImpl<StockDao, StockEntity> implements StockService {

//    @Autowired
//    private StockTradingService stockTradingService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public List<StockEntity> queryStock() {
        StockVo stockVo = new StockVo();
        List<StockEntity> list= this.baseMapper.selectList(new QueryWrapper<StockEntity>());
        return list;

    }
}
