package org.qiyu.live.im.router.provider.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.qiyu.live.im.core.server.interfaces.rpc.IRouterHandlerRpc;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.provider.service.ImRouterService;
import org.springframework.stereotype.Service;

@Service
public class ImRouterServiceImpl implements ImRouterService {

    @DubboReference
    private IRouterHandlerRpc routerHandlerRpc;

    @Override
    public boolean sendMsg(Long userId, ImMsgBody imMsgBody) {
        String objectImServerIp = "192.168.60.1:9095";
        RpcContext.getContext().set("ip", objectImServerIp);
        routerHandlerRpc.sendMsg(imMsgBody);
        return true;
    }
}
