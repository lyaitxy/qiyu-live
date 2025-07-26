package org.qiyu.live.id.generate.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.id.generate.interfaces.IdGenerateRpc;
import org.qiyu.live.id.generate.provider.service.IdGenerateService;

@DubboService
public class IdGenerateRpcImpl implements IdGenerateRpc {

    @Resource
    private IdGenerateService idBuilderService;


    @Override
    public Long getSeqId(Integer code) {
        return idBuilderService.getSeqId(code);
    }

    @Override
    public Long getUnSeqId(Integer code) {
        return idBuilderService.getUnSeqId(code);
    }
}
