package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;

public class ShopCarReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -341133016477720753L;

    private Long userId;
    private Long skuId;
    private Integer roomId;

    @Override
    public String toString() {
        return "ShopCarReqDTO{" +
                "roomId=" + roomId +
                ", userId=" + userId +
                ", skuId=" + skuId +
                '}';
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ShopCarReqDTO(Long userId, Long skuId, Integer roomId) {
        this.roomId = roomId;
        this.skuId = skuId;
        this.userId = userId;
    }
}
