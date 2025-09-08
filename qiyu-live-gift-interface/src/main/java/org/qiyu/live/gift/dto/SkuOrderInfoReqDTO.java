package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class SkuOrderInfoReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -9220028624463964600L;

    private Long id;
    private Long userId;
    private Integer roomId;
    private Integer status;
    private List<Long> skuIdList;

    public SkuOrderInfoReqDTO() {

    }

    @Override
    public String toString() {
        return "SkuOrderInfoReqDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", roomId=" + roomId +
                ", status=" + status +
                ", skuIdList=" + skuIdList +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public List<Long> getSkuIdList() {
        return skuIdList;
    }

    public void setSkuIdList(List<Long> skuIdList) {
        this.skuIdList = skuIdList;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public SkuOrderInfoReqDTO(Long id, Long userId, Integer roomId, Integer status, List<Long> skuIdList) {
        this.id = id;
        this.roomId = roomId;
        this.skuIdList = skuIdList;
        this.status = status;
        this.userId = userId;
    }


}
