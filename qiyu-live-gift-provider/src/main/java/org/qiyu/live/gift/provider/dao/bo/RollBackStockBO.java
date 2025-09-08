package org.qiyu.live.gift.provider.dao.bo;

public class RollBackStockBO {

    private Long userId;
    private Long orderId;

    @Override
    public String toString() {
        return "RollBackStockBO{" +
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
