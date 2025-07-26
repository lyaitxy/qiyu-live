package org.qiyu.live.user.interfaces.dto;

import java.io.Serializable;

public class UserLoginDTO implements Serializable {

    private boolean isLoginSuccess;
    private String desc;
    private Long userId;
    private String token;

    public boolean isLoginSuccess() {
        return isLoginSuccess;
    }

    public void setLoginSuccess(boolean loginSuccess) {
        isLoginSuccess = loginSuccess;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "UserLoginDTO{" +
                "isLoginSuccess=" + isLoginSuccess +
                ", desc='" + desc + '\'' +
                ", userId=" + userId +
                ", token='" + token + '\'' +
                '}';
    }

    public static UserLoginDTO loginError(String desc) {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setLoginSuccess(false);
        userLoginDTO.setDesc(desc);
        return userLoginDTO;
    }

    public static UserLoginDTO loginSuccess(Long userId, String token) {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setLoginSuccess(true);
        userLoginDTO.setToken(token);
        userLoginDTO.setUserId(userId);
        return userLoginDTO;
    }


}
