package org.qiyu.live.living.provider.consumer;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.framework.mq.starter.consumer.RocketMQConsumerProperties;
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.im.core.server.interfaces.dto.ImOfflineDTO;
import org.qiyu.live.im.core.server.interfaces.dto.ImOnlineDTO;
import org.qiyu.live.living.provider.service.ILivingRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class LivingRoomOfflineConsumer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(LivingRoomOfflineConsumer.class);

    @Resource
    private RocketMQConsumerProperties consumerProperties;
    @Resource
    private ILivingRoomService livingRoomService;

    @Override
    public void afterPropertiesSet() throws Exception {
        initConsumer();
    }

    private void initConsumer() throws MQClientException {
        //初始化我们的 RocketMQ 消费者
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setVipChannelEnabled(false);
        defaultMQPushConsumer.setNamesrvAddr(consumerProperties.getNameSrv());
        defaultMQPushConsumer.setConsumerGroup(consumerProperties.getGroupName() + "_" + LivingRoomOfflineConsumer.class.getSimpleName());
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(10);
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        try {
            defaultMQPushConsumer.subscribe(ImCoreServerProviderTopicNames.IM_OFFLINE_TOPIC, "");
            defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for(MessageExt msg : msgs) {
                    ImOfflineDTO imOfflineDTO = JSON.parseObject(new String(msg.getBody()), ImOfflineDTO.class);
                    livingRoomService.userOfflineHandler(imOfflineDTO);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
        defaultMQPushConsumer.start();
        LOGGER.info("mq消费者启动成功，nameSRV is {}", consumerProperties.getNameSrv());
    }
}
