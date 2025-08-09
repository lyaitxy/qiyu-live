package org.qiyu.live.im.core.server.handler.impl;

import com.alibaba.fastjson.JSON;
import io.micrometer.common.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.constants.ImConstants;
import org.qiyu.live.im.constants.ImMsgCodeEnum;
import org.qiyu.live.im.core.server.common.ChannelHandlerContextCache;
import org.qiyu.live.im.core.server.common.ImContextUtils;
import org.qiyu.live.im.core.server.common.ImMsg;
import org.qiyu.live.im.core.server.handler.SimpleHandler;
import org.qiyu.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import org.qiyu.live.im.core.server.interfaces.dto.ImOnlineDTO;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.interfaces.ImTokenRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录消息处理器
 */
@Component
public class LoginMsgHandler implements SimpleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginMsgHandler.class);

    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MQProducer mqProducer;
    /**
     * 想要建立连接的话，需要进行一系列的参数校验
     * 然后参数无误后，验证存储的userId和消息中的userId是否相同，相同才允许建立连接
     * @param ctx
     * @param imMsg
     */
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        // 防止重复连接，若已经连接就不再接受login请求包，当前
        if (ImContextUtils.getUserId(ctx) != null) {
            return;
        }
        byte[] body = imMsg.getBody();
        if (body == null || body.length == 0) {
            ctx.close();
            LOGGER.error("body error, imMsg is {}", imMsg);
            throw new IllegalArgumentException("body error");
        }
        ImMsgBody imMsgBody = JSON.parseObject(new String(body), ImMsgBody.class);
        String token = imMsgBody.getToken();
        Long userIdFromMsg = imMsgBody.getUserId();
        Integer appId = imMsgBody.getAppId();
        if (StringUtils.isEmpty(token) || userIdFromMsg < 10000 || appId < 10000) {
            ctx.close();
            LOGGER.error("param error, imMsg is {}", imMsg);
            throw new IllegalArgumentException("param error");
        }
        Long userId = imTokenRpc.getUserIdByToken(token);
        // 从RPC获取的userId和传递过来的userId相等，则没出现差错，允许建立连接
        if (userId != null && userId.equals(userIdFromMsg)) {
            loginSuccessHandler(ctx, userId, appId, null);
            return;
        }
        // 不允许建立连接
        ctx.close();
        LOGGER.error("token error, imMsg is {}", imMsg);
        throw new IllegalArgumentException("token error");
    }

    /**
     * ws协议用户初次登录的时候发送mq消息，将userId与roomId关联起来，便于直播间聊天消息的推送
     */
    private void sendLoginMQ(Long userId, Integer appId, Integer roomId) {
        ImOnlineDTO imOnlineDTO = new ImOnlineDTO();
        imOnlineDTO.setUserId(userId);
        imOnlineDTO.setAppId(appId);
        imOnlineDTO.setRoomId(roomId);
        imOnlineDTO.setLoginTime(System.currentTimeMillis());
        Message message = new Message();
        message.setTopic(ImCoreServerProviderTopicNames.IM_ONLINE_TOPIC);
        message.setBody(JSON.toJSONString(imOnlineDTO).getBytes());
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[LoginMsgHandler] sendResult is {}", sendResult);
        } catch (Exception e) {
            LOGGER.error("[LoginMsgHandler] error is ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     *  如果用户成功登录，就处理相关记录
     */
    public void loginSuccessHandler(ChannelHandlerContext ctx, Long userId, Integer appId, Integer roomId) {
        // 按照userId保存好相关的channel信息
        ChannelHandlerContextCache.put(userId, ctx);
        // 将userId保存到netty域信息中，用于正常/非正常logout的处理
        ImContextUtils.setUserId(ctx, userId);
        ImContextUtils.setAppId(ctx, appId);
        ImContextUtils.setRoomId(ctx, roomId);
        // 将im消息回写给客户端
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(respBody));
        // 将im服务器的ip+端口地址保存到Redis，以供Router服务取出进行转发
        stringRedisTemplate.opsForValue().set(ImCoreServerConstants.IM_BIND_IP_KEY + appId +":" +userId,
                ChannelHandlerContextCache.getServerIpAddress() + "%" + userId,
                2 * ImConstants.DEFAULT_HEART_BEAT_GAP, TimeUnit.SECONDS);
        LOGGER.info("[LoginMsgHandler] login success, userId is {}, appId is {}", userId, appId);
        ctx.writeAndFlush(respMsg);
        sendLoginMQ(userId, appId, roomId);
    }
}
