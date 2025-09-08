package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;

public class PrepareOrderReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1742445784431200306L;

    private Long userId;
    private Integer roomId;

    @Override
    public String toString() {
        return "PrepareOrderReqDTO{" +
                "roomId=" + roomId +
                ", userId=" + userId +
                '}';
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
