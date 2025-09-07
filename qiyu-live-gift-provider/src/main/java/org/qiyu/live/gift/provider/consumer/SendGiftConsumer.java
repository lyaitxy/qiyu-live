package org.qiyu.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.qiyu.live.bank.dto.AccountTradeReqDTO;
import org.qiyu.live.bank.dto.AccountTradeRespDTO;
import org.qiyu.live.bank.interfaces.IQiyuCurrencyAccountRpc;
import org.qiyu.live.common.interfaces.dto.SendGiftMq;
import org.qiyu.live.common.interfaces.topic.GiftProviderTopicNames;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.framework.mq.starter.consumer.RocketMQConsumerProperties;
import org.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.constants.ImMsgCodeEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.im.router.interfaces.enums.ImMsgBizCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class SendGiftConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendGiftConsumer.class);

    @Resource
    private RocketMQConsumerProperties consumerProperties;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
    @DubboReference
    private IQiyuCurrencyAccountRpc qiyuCurrentAccountRpc;
    @DubboReference
    private ImRouterRpc routerRpc;

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        // 设置namesrv地址
        defaultMQPushConsumer.setNamesrvAddr(consumerProperties.getNameSrv());
        // 声明消费者
        defaultMQPushConsumer.setConsumerGroup(consumerProperties.getGroupName() + "_" + SendGiftConsumer.class.getSimpleName());
        // 每次只拉取一条消息
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(10);
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        defaultMQPushConsumer.subscribe(GiftProviderTopicNames.SEND_GIFT, "");

        defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                SendGiftMq sendGiftMq = JSON.parseObject(new String(msg.getBody()), SendGiftMq.class);
                String cacheKey = cacheKeyBuilder.buildGiftConsumeKey(sendGiftMq.getUuid());
                Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKey, -1, 5, TimeUnit.MINUTES);
                if (!lockStatus){
                    // 代表曾经消费过
                    continue;
                }
                AccountTradeReqDTO accountTradeReqDTO = new AccountTradeReqDTO();
                accountTradeReqDTO.setUserId(sendGiftMq.getUserId());
                accountTradeReqDTO.setNum(sendGiftMq.getPrice());
                AccountTradeRespDTO accountTradeRespDTO = qiyuCurrentAccountRpc.consumeForSendGift(accountTradeReqDTO);
                // 如果余额扣减成功
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
                JSONObject jsonObject = new JSONObject();
                if(accountTradeRespDTO.isSuccess()) {
                    // 触发礼物特效推送功能
                    imMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_SUCCESS.getCode());
                    imMsgBody.setUserId(sendGiftMq.getReceiverId());
                    jsonObject.put("url", sendGiftMq.getUrl());
                    imMsgBody.setData(jsonObject.toJSONString());
                } else {
                    // 利用im将发送失败的消息告知用户
                    imMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_SUCCESS.getCode());
                    imMsgBody.setUserId(sendGiftMq.getUserId());
                    jsonObject.put("msg", accountTradeRespDTO.getMsg());
                    imMsgBody.setData(jsonObject.toJSONString());
                }
                routerRpc.sendMsg(imMsgBody);
                LOGGER.info("[sendGiftConsumer] msg is {}", msg);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        defaultMQPushConsumer.start();
    }
}
