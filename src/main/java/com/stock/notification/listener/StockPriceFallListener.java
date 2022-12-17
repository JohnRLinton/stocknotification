package com.stock.notification.listener;

import com.rabbitmq.client.Channel;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.service.UserAlertService;
import com.stock.notification.vo.StockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author luoyang
 * @email 772767100@qq.com
 */

@RabbitListener(queues = "stock.pricefall.queue")
@Service
@Slf4j
public class StockPriceFallListener {


    @Autowired
    UserAlertService userAlertService;

    @RabbitHandler
    public void listener(StockVo stockVo, Channel channel, Message message) throws IOException {
        log.info("收到股票价格下跌，准备通知用户" + stockVo.getStockCode());
        try {
            //通知用户股票下跌
            userAlertService.notifyUser(stockVo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
