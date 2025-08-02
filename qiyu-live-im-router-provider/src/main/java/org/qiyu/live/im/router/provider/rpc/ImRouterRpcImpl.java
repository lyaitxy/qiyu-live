package org.qiyu.live.im.router.provider.rpc;

import jakarta.annotation.Resource;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.im.router.provider.service.ImRouterService;

public class ImRouterRpcImpl implements ImRouterRpc {

    @Resource
    private ImRouterService routerService;

    @Override
    public boolean sendMsg(Long userId, ImMsgBody imMsgBody) {
        routerService.sendMsg(userId, imMsgBody);
        return true;
    }
}
