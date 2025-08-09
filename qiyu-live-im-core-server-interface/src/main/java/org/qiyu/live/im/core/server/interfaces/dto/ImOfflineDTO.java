package org.qiyu.live.im.core.server.interfaces.dto;

import java.io.Serializable;

public class ImOfflineDTO implements Serializable {

    private Long userId;
    private Integer appId;
    private Integer roomId;
    private long logoutTime;

    @Override
    public String toString() {
        return "ImOfflineDTO{" +
                "appId=" + appId +
                ", userId=" + userId +
                ", roomId=" + roomId +
                ", logoutTime=" + logoutTime +
                '}';
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
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
