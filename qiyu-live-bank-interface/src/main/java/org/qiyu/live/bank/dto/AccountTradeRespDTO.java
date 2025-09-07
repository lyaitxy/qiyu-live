package org.qiyu.live.bank.dto;

import java.io.Serializable;

public class AccountTradeRespDTO implements Serializable {

    private int code;
    private long userId;
    private boolean isSuccess;
    private String msg;

    public static AccountTradeRespDTO buildFail(long userId, String msg, int code) {
        AccountTradeRespDTO accountTradeRespDTO = new AccountTradeRespDTO();
        accountTradeRespDTO.setUserId(userId);
        accountTradeRespDTO.setMsg(msg);
        accountTradeRespDTO.setCode(code);
        accountTradeRespDTO.setSuccess(false);
        return accountTradeRespDTO;
    }
    public static AccountTradeRespDTO buildSuccess(long userId, String msg) {
        AccountTradeRespDTO accountTradeRespDTO = new AccountTradeRespDTO();
        accountTradeRespDTO.setUserId(userId);
        accountTradeRespDTO.setMsg(msg);
        accountTradeRespDTO.setSuccess(true);
        return accountTradeRespDTO;
    }

    @Override
    public String toString() {
        return "AccountTradeRespDTO{" +
                "code=" + code +
                ", userId=" + userId +
                ", isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
