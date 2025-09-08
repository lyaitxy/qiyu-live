package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class ShopCarRespDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7147830236451419334L;

    private Long userId;
    private Integer roomId;
    private List<ShopCarItemRespDTO> skuCarItemRespDTODTOS;

    @Override
    public String toString() {
        return "ShopCarRespDTO{" +
                "roomId=" + roomId +
                ", userId=" + userId +
                ", skuCarItemRespDTODTOS=" + skuCarItemRespDTODTOS +
                '}';
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public List<ShopCarItemRespDTO> getSkuCarItemRespDTODTOS() {
        return skuCarItemRespDTODTOS;
    }

    public void setSkuCarItemRespDTODTOS(List<ShopCarItemRespDTO> skuCarItemRespDTODTOS) {
        this.skuCarItemRespDTODTOS = skuCarItemRespDTODTOS;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
