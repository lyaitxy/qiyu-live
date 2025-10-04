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
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.gift.constants.SendGiftTypeEnum;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.constants.ImMsgCodeEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.im.router.interfaces.enums.ImMsgBizCodeEnum;
import org.qiyu.live.living.dto.LivingRoomReqDTO;
import org.qiyu.live.living.dto.LivingRoomRespDTO;
import org.qiyu.live.living.rpc.ILivingRoomRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private ILivingRoomRpc livingRoomRpc;
    @Resource
    private GiftProviderCacheKeyBuilder giftProviderCacheKeyBuilder;

    private static final Long PK_INIT_NUM = 50L;
    private static final Long PK_MAX_NUM = 100L;
    private static final Long PK_MIN_NUM = 0L;
    private static final DefaultRedisScript<Long> redisScript;

    static {
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setLocation(new ClassPathResource("getPkNumAndSeqId.lua"));
    }

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
                // 获取mq中的送礼信息
                SendGiftMq sendGiftMq = JSON.parseObject(new String(msg.getBody()), SendGiftMq.class);
                String cacheKey = cacheKeyBuilder.buildGiftConsumeKey(sendGiftMq.getUuid());
                // 已经消费过了，不需要再次消费了
                Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKey, -1, 5, TimeUnit.MINUTES);
                if (!lockStatus){
                    // 代表曾经消费过
                    continue;
                }
                AccountTradeReqDTO accountTradeReqDTO = new AccountTradeReqDTO();
                accountTradeReqDTO.setUserId(sendGiftMq.getUserId());
                accountTradeReqDTO.setNum(sendGiftMq.getPrice());
                // 进行余额扣费
                AccountTradeRespDTO accountTradeRespDTO = qiyuCurrentAccountRpc.consumeForSendGift(accountTradeReqDTO);

                // 如果余额扣减成功
                JSONObject jsonObject = new JSONObject();
                Integer sendGiftType = sendGiftMq.getType();
                if(accountTradeRespDTO.isSuccess()) {
                    // 查询在直播间的userId
                    LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
                    livingRoomReqDTO.setRoomId(sendGiftMq.getRoomId());
                    livingRoomReqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
                    List<Long> userIdList = livingRoomRpc.queryUserIdsByRoomId(livingRoomReqDTO);
                    if(SendGiftTypeEnum.DEFAULT_SEND_GIFT.getCode().equals(sendGiftType)) {
                        // 触发礼物特效推送功能
                        jsonObject.put("url", sendGiftMq.getUrl());
                        this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_SUCCESS.getCode(), jsonObject);
                        LOGGER.info("[sendGiftConsumer] send success");
                    } else if(sendGiftType.equals(SendGiftTypeEnum.PK_SEND_GIFT.getCode())) {
                        // pk类型的送礼，要通知什么给直播间的用户
                        // url 礼物特效全直播间可见
                        // todo 进度条全直播间可见
                        // pk进度条的实现
                        // 使用Redis以roomId为单位存储当前直播间的 PK进度值，并存储当前送礼的消息有序id
                        // 避免发送消息乱序造成前端显示之前的消息回调的进度条值
                        // 直播间pk进度以roomID为维度，送礼（A）incr,(B)decr
                        pkImMsgSend(sendGiftMq, jsonObject, sendGiftMq.getRoomId(), userIdList);
                        LOGGER.info("[sendGiftConsumer] PK Gift send success");
                    }
                } else {
                    // 利用im将发送失败的消息告知用户
                    jsonObject.put("msg", accountTradeRespDTO.getMsg());
                    this.sendImMsgSingleton(sendGiftMq.getUserId(), ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_FAIL.getCode(), jsonObject);
                    LOGGER.info("[sendGiftConsumer] send fail, msg is {}", accountTradeRespDTO.getMsg());
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        defaultMQPushConsumer.start();
    }

    /**
     * 单独通知送礼对象
     */
    private void sendImMsgSingleton(Long userId, Integer bizCode, JSONObject jsonObject) {
        ImMsgBody imMsgBody = new ImMsgBody();
        imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        imMsgBody.setBizCode(bizCode);
        imMsgBody.setUserId(userId);
        imMsgBody.setData(jsonObject.toJSONString());
        routerRpc.sendMsg(imMsgBody);
    }

    /**
     * 批量发送im消息
     */
    private void batchSendImMsg(List<Long> userIdList, Integer bizCode, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(bizCode);
            imMsgBody.setData(jsonObject.toJSONString());
            imMsgBody.setUserId(userId);
            return imMsgBody;
        }).toList();
        routerRpc.batchSendMsg(imMsgBodies);
    }

    /**
     * PK直播间送礼扣费成功后的流程：
     * 1. 设置礼物特效url
     * 2. 设置PK进度条的值
     * 3. 批量推送给直播间全部用户
     */
    private void pkImMsgSend(SendGiftMq sendGiftMq, JSONObject jsonObject, Integer roomId, List<Long> userIdList) {
        // PK送礼，要求全体可见
        // 1. TODO PK进度条全直播间可见
        String cacheKey = cacheKeyBuilder.buildLivingPkIsOver(roomId);
        // 1.1 判断直播PK是否已经结束
        Boolean isOver = redisTemplate.hasKey(cacheKey);
        if (Boolean.TRUE.equals(isOver)) {
            return;
        }
        // 1.2 获取 pkUserId 和 pkObjId
        Long pkObjId = livingRoomRpc.queryOnlinePkUserId(roomId);
        LivingRoomRespDTO livingRoomRespDTO = livingRoomRpc.queryByRoomId(roomId);
        if (pkObjId == null || livingRoomRespDTO == null || livingRoomRespDTO.getAnchorId() == null) {
            LOGGER.error("[sendGiftConsumer] 两个用户已经有不在线的， roomID is {}", roomId);
            return;
        }
        Long pkUserId = livingRoomRespDTO.getAnchorId();
        // 1.3 获取当前进度条值 和 序列号
        Long pkNum = 0L;
        // 获取该条消息的序列号，避免消息乱序
        long sendGiftSeqNum = System.currentTimeMillis();
        // 这个是当前pk直播间的缓存key
        String pkNumKey = cacheKeyBuilder.buildLivingPkKey(roomId);
        if (sendGiftMq.getReceiverId().equals(pkUserId)) {
            // 收礼人是房主的话，进度条增加
            int moveStep = sendGiftMq.getPrice() / 10;
            pkNum = redisTemplate.execute(redisScript, Collections.singletonList(pkNumKey), PK_INIT_NUM, PK_MAX_NUM, PK_MIN_NUM, moveStep);
            if (PK_MAX_NUM <= pkNum) {
                jsonObject.put("winnerId", pkUserId);
            }
        } else if (sendGiftMq.getReceiverId().equals(pkObjId)) {
            // 收礼人是挑战方，进度条减少
            int moveStep = sendGiftMq.getPrice() / 10 * -1;
            redisTemplate.execute(redisScript, Collections.singletonList(pkNumKey), PK_INIT_NUM, PK_MAX_NUM, PK_MIN_NUM, moveStep);
            if (PK_MIN_NUM >= pkNum) {
                jsonObject.put("winnerId", pkObjId);
            }
        }
        jsonObject.put("receiverId", sendGiftMq.getReceiverId());
        jsonObject.put("sendGiftSeqNum", sendGiftSeqNum);
        jsonObject.put("pkNum", pkNum);
        // 2 礼物特效url全直播间可见
        jsonObject.put("url", sendGiftMq.getUrl());
        // 3 搜索要发送的用户
        // 利用封装方法发送批量消息，bizCode为PK_SEND_SUCCESS
        this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_PK_SEND_GIFT_SUCCESS.getCode(), jsonObject);
    }
}
