package org.qiyu.live.api.service.impl;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.ImService;
import org.qiyu.live.api.vo.resp.ImConfigVO;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.interfaces.ImTokenRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ImServiceImpl implements ImService {

    @DubboReference
    private ImTokenRpc imTokenRpc;
    @Resource
    private DiscoveryClient discoveryClient;

    /**
     * 通过nacos 的 DiscoveryClient 获取Nacos中注册信息
     * @return
     */
    @Override
    public ImConfigVO getImConfig() {
        ImConfigVO imConfigVO = new ImConfigVO();
        imConfigVO.setToken(imTokenRpc.createImLoginToken(QiyuRequestContext.getUserId(), AppIdEnum.QIYU_LIVE_BIZ.getCode()));
        // 获取到在Nacos 中注册的对应服务名的示例集合
        List<ServiceInstance> instances = discoveryClient.getInstances("qiyu-live-im-core-server");
        Collections.shuffle(instances);
        ServiceInstance serviceInstance = instances.get(0);
        imConfigVO.setTcpImServerAddress(serviceInstance.getHost() + ":8085");
        imConfigVO.setWsImServerAddress(serviceInstance.getHost() + ":8086");
        return imConfigVO;
    }
}
