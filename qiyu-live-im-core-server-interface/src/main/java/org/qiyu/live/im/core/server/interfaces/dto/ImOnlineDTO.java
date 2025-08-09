package org.qiyu.live.im.core.server.interfaces.dto;

import java.io.Serializable;

public class ImOnlineDTO implements Serializable {

    private Long userId;
    private Integer appId;
    private Integer roomId;
    private long loginTime;

    @Override
    public String toString() {
        return "ImOnlineDTO{" +
                "appId=" + appId +
                ", userId=" + userId +
                ", roomId=" + roomId +
                ", loginTime=" + loginTime +
                '}';
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
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
