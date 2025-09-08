package org.qiyu.live.gift.provider.service;

import org.qiyu.live.gift.dto.RollBackStockDTO;
import org.qiyu.live.gift.provider.dao.po.SkuStockInfoPO;

import java.util.List;

public interface ISkuStockInfoService {

    /**
     * 根据stuId跟新库存之
     */
    boolean updateStockNum(Long skuId, Integer stockNum);

    /**
     * 根据stuId扣减库存值
     */
    boolean decrStockNumBySkuId(Long skuId, Integer num);

    /**
     * 使用lua脚本扣减缓存的库存值
     */
    boolean decrStockNumBySkuIdByLua(Long skuId, Integer num);

    /**
     * 根据skuId查询库存值
     */
    SkuStockInfoPO queryBySkuId(Long skuId);

    /**
     * 根据stuIdList批量查询数据
     */
    List<SkuStockInfoPO> queryBySkuIds(List<Long> skuIdList);

    /**
     * 处理库存回滚逻辑
     */
    void stockRollBackHandler(RollBackStockDTO rollBackStockDTO);
}
