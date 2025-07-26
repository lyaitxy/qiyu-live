package org.qiyu.live.id.generate.interfaces;

public interface IdGenerateRpc {
    // 获取有序id
    Long getSeqId(Integer code);
    // 获取无序id
    Long getUnSeqId(Integer code);

}
