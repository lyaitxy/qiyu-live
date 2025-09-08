package org.qiyu.live.api.vo.resp;

import org.qiyu.live.gift.dto.ShopCarItemRespDTO;

import java.util.List;

public class ShopCarRespVO {

    private Long userId;
    private Integer roomId;
    private List<ShopCarItemRespDTO> shopCarItemRespDTOS;

    @Override
    public String toString() {
        return "ShopCarRespVO{" +
                "roomId=" + roomId +
                ", userId=" + userId +
                ", shopCarItemRespDTOS=" + shopCarItemRespDTOS +
                '}';
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public List<ShopCarItemRespDTO> getShopCarItemRespDTOS() {
        return shopCarItemRespDTOS;
    }

    public void setShopCarItemRespDTOS(List<ShopCarItemRespDTO> shopCarItemRespDTOS) {
        this.shopCarItemRespDTOS = shopCarItemRespDTOS;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
