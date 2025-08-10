package org.qiyu.live.gift.dto;

import java.io.Serializable;
import java.util.Date;

public class GiftRecordDTO implements Serializable {

    private Long id;
    /**
     * 发送人
     */
    private Long userId;
    /**
     * 收礼人
     */
    private Long objectId;
    /**
     * 礼物来源
     */
    private Integer source;
    /**
     * 送礼金额
     */
    private Integer price;
    /**
     * 送礼金额的单位
     */
    private Integer priceUnit;
    /**
     * 礼物id
     */
    private Integer giftId;
    /**
     * 发送时间
     */
    private Date sendTime;

    @Override
    public String toString() {
        return "GiftRecordDTO{" +
                "giftId=" + giftId +
                ", id=" + id +
                ", userId=" + userId +
                ", objectId=" + objectId +
                ", source=" + source +
                ", price=" + price +
                ", priceUnit=" + priceUnit +
                ", sendTime=" + sendTime +
                '}';
    }

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(Integer priceUnit) {
        this.priceUnit = priceUnit;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
