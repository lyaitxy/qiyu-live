package org.qiyu.live.msg.provider.consumer.handler.impl;

import com.alibaba.fastjson.JSON;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.living.dto.LivingRoomReqDTO;
import org.qiyu.live.living.rpc.ILivingRoomRpc;
import org.qiyu.live.msg.dto.MessageDTO;
import org.qiyu.live.im.router.interfaces.enums.ImMsgBizCodeEnum;
import org.qiyu.live.msg.provider.consumer.handler.MessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleMessageHandlerImpl implements MessageHandler {

    @DubboReference
    private ImRouterRpc routerRpc;
    @DubboReference
    private ILivingRoomRpc livingRoomRpc;

    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        int bizCode = imMsgBody.getBizCode();
        // 直播间的聊天消息
        if(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode() == bizCode) {
            // 一个人发送，n个人接收
            // 根据roomId去调用rpc方法查询直播间在线userId
            MessageDTO messageDTO = JSON.parseObject(imMsgBody.getData(), MessageDTO.class);
            Integer roomId = messageDTO.getRoomId();
            LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
            livingRoomReqDTO.setRoomId(roomId);
            livingRoomReqDTO.setAppId(imMsgBody.getAppId());
            List<Long> userIdList = livingRoomRpc.queryUserIdsByRoomId(livingRoomReqDTO).stream().filter(x -> !x.equals(imMsgBody.getUserId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(userIdList)) {
                System.out.println("[SingleMessageHandlerImpl] 要转发的userIdList为空");
                return;
            }
            List<ImMsgBody> imMsgBodies = new ArrayList<>();
            userIdList.forEach(userId -> {
                ImMsgBody respMsgBody = new ImMsgBody();
                //这里的userId设置的是objectId，因为是发送给对方客户端
                respMsgBody.setUserId(userId);
                respMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
                respMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
                respMsgBody.setData(JSON.toJSONString(messageDTO));
                // 设置发送目标对象的id
                respMsgBody.setUserId(userId);
                imMsgBodies.add(respMsgBody);
            });
            //将消息推送给router进行转发给im服务器
            routerRpc.batchSendMsg(imMsgBodies);
        }
    }
}
