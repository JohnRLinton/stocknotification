package com.stock.notification.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;



/**
 * @author luoyang
 * @email 772767100@qq.com
 */
@Configuration
public class MyRabbitMQConfig {

    /* 容器中的Queue、Exchange、Binding 会自动创建（在RabbitMQ）不存在的情况下 */


    /**
     * 股票价格上涨队列
     *
     * @return
     */
    @Bean
    public Queue stockPriceRiseQueue() {

        Queue queue = new Queue("stock.pricerise.queue", true, false, false);

        return queue;
    }

    /**
     * 股票价格涨超队列
     *
     * @return
     */
    @Bean
    public Queue stockPriceRiseOverQueue() {

        Queue queue = new Queue("stock.priceriseover.queue", true, false, false);

        return queue;
    }

    /**
     * 股票价格下跌队列
     *
     * @return
     */
    @Bean
    public Queue stockPriceFallQueue() {

        Queue queue = new Queue("stock.pricefall.queue", true, false, false);

        return queue;
    }

    /**
     * 股票价格跌超队列
     *
     * @return
     */
    @Bean
    public Queue stockPriceFallOverQueue() {

        Queue queue = new Queue("stock.pricefallover.queue", true, false, false);

        return queue;
    }


    /**
     * TopicExchange
     *
     * @return
     */
    @Bean
    public Exchange stockEventExchange() {
        /*
         *   String name,
         *   boolean durable,
         *   boolean autoDelete,
         *   Map<String, Object> arguments
         * */
        return new TopicExchange("stock-event-exchange", true, false);

    }


    @Bean
    public Binding stockRiseBinding() {
        /*
         * String destination, 目的地（队列名或者交换机名字）
         * DestinationType destinationType, 目的地类型（Queue、Exhcange）
         * String exchange,
         * String routingKey,
         * Map<String, Object> arguments
         * */
        return new Binding("stock.pricerise.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.price",
                null);
    }

    @Bean
    public Binding stockRiseOverBinding() {

        return new Binding("stock.priceriseover.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.priceover",
                null);
    }

    /**
     *
     * @return
     */
    @Bean
    public Binding stockFallBinding() {

        return new Binding("stock.pricefall.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.fall",
                null);
    }


    @Bean
    public Binding stockFallOverBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments
        Binding binding = new Binding(
                "stock.pricefallover.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.fallover",
                null);

        return binding;
    }


}

