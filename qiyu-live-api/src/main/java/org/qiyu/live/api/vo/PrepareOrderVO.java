package org.qiyu.live.api.vo;

public class PrepareOrderVO {

    private Long userId;
    private Integer roomId;

    @Override
    public String toString() {
        return "PrepareOrderVO{" +
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
