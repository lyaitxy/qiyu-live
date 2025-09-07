package org.qiyu.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.qiyu.live.common.interfaces.topic.GiftProviderTopicNames;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.framework.mq.starter.consumer.RocketMQConsumerProperties;
import org.qiyu.live.gift.provider.dao.bo.SendRedPacketBO;
import org.qiyu.live.gift.provider.service.IRedPacketConfigService;
import org.qiyu.live.im.dto.ImMsgBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceiveRedPacketConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveRedPacketConsumer.class);

    @Resource
    private IRedPacketConfigService redPacketConfigService;
    @Resource
    private RocketMQConsumerProperties consumerProperties;


    @Override
    public void afterPropertiesSet() throws Exception {
        //初始化我们的 RocketMQ 消费者
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        // 设置namesrv地址
        defaultMQPushConsumer.setNamesrvAddr(consumerProperties.getNameSrv());
        // 声明消费者
        defaultMQPushConsumer.setConsumerGroup(consumerProperties.getGroupName() + "_" + ReceiveRedPacketConsumer.class.getSimpleName());
        // 每次只拉取一条消息
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        defaultMQPushConsumer.subscribe(GiftProviderTopicNames.RECEIVE_RED_PACKET, "");

        defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            String json = new String(msgs.get(0).getBody());
            SendRedPacketBO sendRedPacketBO = JSON.parseObject(json, SendRedPacketBO.class);
            redPacketConfigService.receiveRedPacketHandler(sendRedPacketBO.getReqDTO(), sendRedPacketBO.getPrice());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        defaultMQPushConsumer.start();
        LOGGER.info("mq消费者启动成功，nameSRV is {}", consumerProperties.getNameSrv());
    }
}
