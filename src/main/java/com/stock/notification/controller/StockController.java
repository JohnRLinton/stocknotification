package com.stock.notification.controller;


//import com.baomidou.mybatisplus.extension.api.R;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.entity.UserEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import com.stock.notification.service.UserAlertService;
import com.stock.notification.service.UserStockRelationService;
import com.stock.notification.vo.StockVo;
import com.stock.notification.vo.UserAlertVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.stock.notification.utils.R;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 股票自选模块
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

    @Autowired
    private UserAlertService userAlertService;

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
    public R deleteShares(@PathVariable("userId") int userId,@PathVariable("stockCode") String stockCode){
        stockService.removeShares(stockCode,userId);
        return R.ok();
    }


    /**
     * 查看股票交易信息
     * @param stockCode
     * @return
     */
    @RequestMapping("/viewStockTrading")
    public R viewStockTrading(@PathVariable("stockCode") String stockCode){
        Map<String, StocktradingEntity> stocktradingEntityMap=stockTradingService.getStockTradingJson(stockCode);
        StocktradingEntity stocktradingEntity = stocktradingEntityMap.get(stockCode);
        return R.ok().put("StocktradingData",stocktradingEntity);
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

    /**
     *
     * @param userId
     * @param stockCode
     * @param alertType
     * @param alertContent
     * @param alertFrequency
     * @return
     */
    @RequestMapping("/addAlert")
    public R addAlert(@PathVariable("userId") int userId, @PathVariable("stockCode") String stockCode, @PathVariable("alertType") int alertType, @PathVariable("alertContent") BigDecimal alertContent, @PathVariable("alertFrequency")int alertFrequency){
        userAlertService.addAlert(userId,stockCode,alertType,alertContent,alertFrequency);
        StockVo stockVo = stockTradingService.monitorStockChange(stockCode);
        UserAlertVo userAlertVo=userAlertService.notifyUser(stockVo);
        return R.ok().put("userAlertData",userAlertVo);
    }

//    @RequestMapping("/notifyUser")
//    public R notifyUser(){
//
//    }
}
