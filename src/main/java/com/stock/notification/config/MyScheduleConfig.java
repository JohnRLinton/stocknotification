package com.stock.notification.config;

import com.stock.notification.dao.StockTradingDao;
import com.stock.notification.entity.StocktradingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class MyScheduleConfig {

    @Resource
    private StockTradingDao stockTradingDao;

    //3.添加定时任务
    @Scheduled(cron = "0/5 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    //@Scheduled(fixedRate=5000)
    private void configureTasks() {
        System.err.println("执行静态定时任务时间: " + LocalDateTime.now());

        //执行动态定时任务
        StocktradingEntity stocktradingEntity = new StocktradingEntity();
        stocktradingEntity.setStockCode("00700");
        stocktradingEntity.setLatestPrice(new BigDecimal(600));
        stockTradingDao.updateById(stocktradingEntity);

        log.info("定时任务执行完成");
    }
}
