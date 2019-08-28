package com.luopo.goupiao.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {
    public static final String GOUPIAO_QUEUE = "goupiao.queue";

    //Direct模式
    //返回一个名为GOUPIAO_QUEUE的队列bean
    @Bean
    public Queue getGoupiaoQueue() {
        return new Queue(GOUPIAO_QUEUE, true);
    }
}
