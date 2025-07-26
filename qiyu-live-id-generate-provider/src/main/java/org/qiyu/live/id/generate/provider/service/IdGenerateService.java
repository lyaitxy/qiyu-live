package org.qiyu.live.id.generate.provider.service;

public interface IdGenerateService {
    // 获取有序id
    Long getSeqId(Integer code);
    // 获取无序id
    Long getUnSeqId(Integer code);

}
