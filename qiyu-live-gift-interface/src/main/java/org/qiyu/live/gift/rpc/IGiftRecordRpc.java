package org.qiyu.live.gift.rpc;

import org.qiyu.live.gift.dto.GiftRecordDTO;

public interface IGiftRecordRpc {
    /**
     * 插入一条送礼记录
     */
    void insertOne(GiftRecordDTO giftRecordDTO);
}
