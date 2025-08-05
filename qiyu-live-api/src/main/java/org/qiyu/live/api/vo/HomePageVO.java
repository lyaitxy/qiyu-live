package org.qiyu.live.api.vo;

public class HomePageVO {

    private boolean loginStatus;
    private long userId;
    private String nickName;
    private String avatar;
    // 是否是主播身份
    private boolean showStartLivingBtn;

    @Override
    public String toString() {
        return "HomePageVO{" +
                "avatar='" + avatar + '\'' +
                ", loginStatus=" + loginStatus +
                ", userId=" + userId +
                ", nickName='" + nickName + '\'' +
                ", showStartLivingBtn=" + showStartLivingBtn +
                '}';
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isShowStartLivingBtn() {
        return showStartLivingBtn;
    }

    public void setShowStartLivingBtn(boolean showStartLivingBtn) {
        this.showStartLivingBtn = showStartLivingBtn;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
