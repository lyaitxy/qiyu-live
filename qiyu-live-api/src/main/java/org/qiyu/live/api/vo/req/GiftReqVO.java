package org.qiyu.live.api.vo.req;

public class GiftReqVO {

    /**
     * 礼物id
     */
    private int giftId;
    /**
     * 直播间id
     */
    private Integer roomId;
    /**
     * 送礼人id
     */
    private Long senderUserId;
    /**
     * 接受人id
     */
    private Long receiverId;
    /**
     * 玩法类型，PK等
     */
    private Integer type;

    @Override
    public String toString() {
        return "GiftReqVO{" +
                "giftId=" + giftId +
                ", roomId=" + roomId +
                ", senderUserId=" + senderUserId +
                ", receiverId=" + receiverId +
                ", type=" + type +
                '}';
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
