package org.qiyu.live.im.core.server.interfaces.rpc;

import org.qiyu.live.im.dto.ImMsgBody;

import java.util.List;

/**
 * 专门给Router层的服务进行调用接口
 */
public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息发送
     */
    void sendMsg(ImMsgBody imMsgBody);

    /**
     * 批量发送消息
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}
