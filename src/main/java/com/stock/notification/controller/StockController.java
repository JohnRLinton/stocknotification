package com.stock.notification.controller;


import com.baomidou.mybatisplus.extension.api.R;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author luoyang
 * @email 772767100@qq.com
 * @date 2022-12-7 16:50
 */
@RestController
@RequestMapping("stock/userSelect")

public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 查出所有分类以及子分类，以树形结构组装起来
     */
    @RequestMapping("/viewAllShares")
    public R stocklist(){

        List<StockEntity> stockEntities = stockService.queryStock();

        return R.ok().put("stockdata", stockEntities);
    }
}
