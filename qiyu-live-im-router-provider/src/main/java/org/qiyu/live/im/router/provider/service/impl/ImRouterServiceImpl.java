package org.qiyu.live.im.router.provider.service.impl;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.qiyu.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import org.qiyu.live.im.core.server.interfaces.rpc.IRouterHandlerRpc;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.provider.service.ImRouterService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImRouterServiceImpl implements ImRouterService {

    @DubboReference
    private IRouterHandlerRpc routerHandlerRpc;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {
        // bindAddress是dubbo的地址和端口
        String bindAddress = stringRedisTemplate.opsForValue().get(ImCoreServerConstants.IM_BIND_IP_KEY + imMsgBody.getAppId() + ":" + imMsgBody.getUserId());
        if(StringUtils.isEmpty(bindAddress)) {
            return false;
        }
        bindAddress = bindAddress.substring(0, bindAddress.indexOf("%"));
        // RpcContext是线程级上下文对象,用于在消费者和服务提供者之间传递隐式信息
        RpcContext.getContext().set("ip", bindAddress);
        routerHandlerRpc.sendMsg(imMsgBody);
        return true;
    }

    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodyList) {
        // 需要对id进行分组，对相同服务器的UserIdList进行分组，每组进行一次调用，减少网络开销
        String cacheKeyPrefix = ImCoreServerConstants.IM_BIND_IP_KEY + imMsgBodyList.get(0).getAppId() + ":";
        List<String> cacheKeyList = imMsgBodyList.stream().map(ImMsgBody::getUserId).map(userId -> cacheKeyPrefix + userId).collect(Collectors.toList());
        // 批量取出每个用户绑定的ip地址
        List<String> ipList = stringRedisTemplate.opsForValue().multiGet(cacheKeyList);
        Map<String, List<Long>> userIdMap = new HashMap<>();
        ipList.forEach(ip -> {
            String currentIp = ip.substring(0, ip.indexOf("%"));
            Long userId = Long.valueOf(ip.substring(ip.indexOf("%") + 1));

            List<Long> currentUserIdList = userIdMap.getOrDefault(currentIp, new ArrayList<Long>());
            currentUserIdList.add(userId);
            userIdMap.put(currentIp, currentUserIdList);
        });
        //根据注册IP对ImMsgBody进行分组
        //将连接到同一台ip地址的ImMsgBody组装到一个List中，进行统一发送
        Map<Long, ImMsgBody> userIdMsgMap = imMsgBodyList.stream().collect(Collectors.toMap(ImMsgBody::getUserId, body -> body));
        for (Map.Entry<String, List<Long>> entry : userIdMap.entrySet()) {
            //设置dubbo RPC上下文
            RpcContext.getContext().set("ip", entry.getKey());
            List<Long> currentUserIdList = entry.getValue();
            List<ImMsgBody> batchSendMsgBodyGroupByIpList = currentUserIdList.stream().map(userIdMsgMap::get).collect(Collectors.toList());
            routerHandlerRpc.batchSendMsg(batchSendMsgBodyGroupByIpList);
        }
    }
}
