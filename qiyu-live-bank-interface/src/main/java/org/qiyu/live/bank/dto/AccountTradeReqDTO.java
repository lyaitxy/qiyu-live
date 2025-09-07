package org.qiyu.live.bank.dto;

import java.io.Serializable;

public class AccountTradeReqDTO implements Serializable {

    private long userId;
    private int num;

    @Override
    public String toString() {
        return "AccountTradeReqDTO{" +
                "num=" + num +
                ", userId=" + userId +
                '}';
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
