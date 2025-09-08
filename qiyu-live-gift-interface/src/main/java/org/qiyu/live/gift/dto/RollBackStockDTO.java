package org.qiyu.live.gift.dto;

import java.io.Serializable;

public class RollBackStockDTO implements Serializable {

    private Long orderId;
    private Long userId;

    @Override
    public String toString() {
        return "RollBackStockDTO{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                '}';
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
