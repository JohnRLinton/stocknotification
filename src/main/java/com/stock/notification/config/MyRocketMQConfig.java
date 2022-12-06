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
public class MyRocketMQConfig {

    /* 容器中的Queue、Exchange、Binding 会自动创建（在RabbitMQ）不存在的情况下 */

    /**
     * 死信队列
     *
     * @return
     */@Bean
    public Queue stockDelayQueue() {
        /*
            Queue(String name,  队列名字
            boolean durable,  是否持久化
            boolean exclusive,  是否排他
            boolean autoDelete, 是否自动删除
            Map<String, Object> arguments) 属性
         */
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release.stock");
        arguments.put("x-message-ttl", 60000); // 消息过期时间 1分钟
        Queue queue = new Queue("stock.alert.queue", true, false, false, arguments);

        return queue;
    }

    /**
     * 普通队列
     *
     * @return
     */
    @Bean
    public Queue stockReleaseQueue() {

        Queue queue = new Queue("stock.release.queue", true, false, false);

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
    public Binding stockCreateBinding() {
        /*
         * String destination, 目的地（队列名或者交换机名字）
         * DestinationType destinationType, 目的地类型（Queue、Exhcange）
         * String exchange,
         * String routingKey,
         * Map<String, Object> arguments
         * */
        return new Binding("stock.alert.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.create.stock",
                null);
    }

    @Bean
    public Binding stockReleaseBinding() {

        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.stock",
                null);
    }

    /**
     *
     * @return
     */
    @Bean
    public Binding stockReleaseOtherBinding() {

        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.other.#",
                null);
    }


    /**
     *
     * @return
     */
    @Bean
    public Queue stockSecKillOrrderQueue() {
        Queue queue = new Queue("stock.xxx.queue", true, false, false);
        return queue;
    }

    @Bean
    public Binding stockSecKillOrrderQueueBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments
        Binding binding = new Binding(
                "stock.seckill.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.seckill.stock",
                null);

        return binding;
    }


}

