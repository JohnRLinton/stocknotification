package com.stock.notification.listener;

import com.rabbitmq.client.Channel;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.service.UserAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.IOException;

/**
 * @author luoyang
 * @email 772767100@qq.com
 */

@RabbitListener(queues = "stock.priceriseover.queue")
@Service
@Slf4j
public class StockPriceRiseOverListener {


    @Autowired
    UserAlertService userAlertService;

    @RabbitHandler
    public void listener(StockEntity stockEntity, Channel channel, Message message) throws IOException {
        log.info("收到股票价格涨超，准备通知用户" + stockEntity.getStockCode());
        try {
            //通知用户股票涨超
            userAlertService.notifyRiseOver(stockEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}

