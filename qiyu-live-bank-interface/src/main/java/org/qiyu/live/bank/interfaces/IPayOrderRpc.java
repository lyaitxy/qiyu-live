package org.qiyu.live.bank.interfaces;

import org.qiyu.live.bank.dto.PayOrderDTO;

public interface IPayOrderRpc {

    /**
     *插入订单 ，返回orderId
     */
    String insertOne(PayOrderDTO payOrderDTO);

    /**
     * 根据主键id更新订单状态
     */
    boolean updateOrderStatus(Long id, Integer status);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(String orderId, Integer status);

    /**
     * 支付回调需要请求该接口
     * @param payOrderDTO
     * @return
     */
    boolean payNotify(PayOrderDTO payOrderDTO);
}
