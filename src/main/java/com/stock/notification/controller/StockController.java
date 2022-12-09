package com.stock.notification.controller;


//import com.baomidou.mybatisplus.extension.api.R;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.UserEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import com.stock.notification.service.UserStockRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.stock.notification.utils.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author luoyang
 * @email 772767100@qq.com
 * @date 2022-12-7 16:50
 */

@RestController
@RequestMapping("/stock")

public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockTradingService stockTradingService;

    @Autowired
    private UserStockRelationService userStockRelationService;

    /**
     * 查出自选页面下用户所持股与推荐股
     * @return
     */
    @RequestMapping("/userSelect")
    public R stocklist(@PathVariable("userId") int userId){
        Map<String,List<StockEntity>> stockMap = stockService.queryStock(userId);
        return R.ok().put("stockdata", stockMap);
    }

    /**
     * 添加自选股
     * @param userId
     * @param stockCode
     * @return
     */
    @RequestMapping("/addShares")
    public R addShares(@PathVariable("userId") int userId,@PathVariable("stockCode") String stockCode){
        stockService.addShares(stockCode,userId);
        return R.ok();
    }


    /**
     * 收藏自选股
     * @param userId
     * @param stockCode
     * @return
     */
    @RequestMapping("/collectShares")
    public R collectShares(@PathVariable("userId") int userId,@PathVariable("stockCode") String stockCode){
        stockService.collectShares(stockCode,userId);
        return R.ok();
    }


    /**
     * 删除自选股
     * @param userId
     * @param stockCode
     * @return
     */
    @RequestMapping("/deleteShares")
    public R delete(@PathVariable("userId") int userId,@PathVariable("stockCode") String stockCode){
        stockService.removeShares(stockCode,userId);
        return R.ok();
    }

    /**
     * 查看单个股票下所收藏的全部用户
     * @param stockCode
     * @return
     */
    @RequestMapping("/viewUserByStock")
    public R viewUserByStock(@PathVariable("stockCode") String stockCode){
        List<UserEntity> userList = userStockRelationService.queryUserByStock(stockCode);
        return R.ok().put("userdata",userList);
    }
}
