package com.stock.notification.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.service.UserAlertService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.IOException;

@RabbitListener(queues = "stock.transaction.queue")
@Service
@Slf4j
public class StockTransactionListener {
    @RabbitHandler
    public void listener(StockEntity stockEntity, Channel channel, Message message) throws IOException {
        log.info("收到股票交易信息变动，准备通知用户" + stockEntity.getStockCode());
        try {
            //TODO:编写逻辑方法
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
