package com.luopo.goupiao.mq;

import com.luopo.goupiao.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    AmqpTemplate amqpTemplate ;

    //将消息（String）放入名为GOUPIAO_QUEUE的队列中
    public void sendGoupiaoMessage(GoupiaoMessage goupiaoMessage) {
        String str = RedisService.beanToString(goupiaoMessage);
        log.info("  >> send message : "+str);
        amqpTemplate.convertAndSend(MQConfig.GOUPIAO_QUEUE, str);
    }
}
