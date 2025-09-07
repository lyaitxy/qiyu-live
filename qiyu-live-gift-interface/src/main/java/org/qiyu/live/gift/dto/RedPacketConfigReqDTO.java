package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;

public class RedPacketConfigReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5117539613836783248L;
    private Integer id;
    private Integer roomId;
    private Integer status;
    private Long userId;
    private String redPacketConfigCode;
    private Integer totalPrice;
    private Integer totalCount;
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRedPacketConfigCode() {
        return redPacketConfigCode;
    }

    public void setRedPacketConfigCode(String redPacketConfigCode) {
        this.redPacketConfigCode = redPacketConfigCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
