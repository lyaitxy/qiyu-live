package org.qiyu.live.msg.provider.consumer.handler.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.msg.dto.MessageDTO;
import org.qiyu.live.msg.enums.ImMsgBizCodeEnum;
import org.qiyu.live.msg.provider.consumer.handler.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class SingleMessageHandlerImpl implements MessageHandler {

    @DubboReference
    private ImRouterRpc routerRpc;

    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        int bizCode = imMsgBody.getBizCode();
        // 直播间的聊天消息
        if(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode() == bizCode) {
            MessageDTO messageDTO = JSON.parseObject(imMsgBody.getData(), MessageDTO.class);
            ImMsgBody respMsgBody = new ImMsgBody();
            //这里的userId设置的是objectId，因为是发送给对方客户端
            respMsgBody.setUserId(messageDTO.getObjectId());
            respMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
            respMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("senderId", messageDTO.getUserId());
            jsonObject.put("content,", messageDTO.getContent());
            respMsgBody.setData(jsonObject.toJSONString());
            //将消息推送给router进行转发给im服务器
            routerRpc.sendMsg(respMsgBody);
        }
    }
}
