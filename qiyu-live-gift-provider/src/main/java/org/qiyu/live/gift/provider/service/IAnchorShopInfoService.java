package org.qiyu.live.gift.provider.service;

import java.util.List;

public interface IAnchorShopInfoService {

    /**
     * 根据主播id查询SkuId列表
     */
    List<Long> querySkuIdsByAnchorId(Long anchorId);

    /**
     * 查询出所有有效的主播id列表
     */
    List<Long> queryAllValidAnchorId();
}
