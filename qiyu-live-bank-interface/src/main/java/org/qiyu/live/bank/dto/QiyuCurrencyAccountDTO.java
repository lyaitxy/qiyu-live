package org.qiyu.live.bank.dto;

import java.io.Serializable;
import java.util.Date;

public class QiyuCurrencyAccountDTO implements Serializable {

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 当前余额
     */
    private int currentBalance;
    /**
     * 累计充值
     */
    private int totalCharged;
    /**
     * 账户状态(0无效 1有效 2冻结)
     */
    private Integer status;
    private Date createTime;
    private Date updateTime;

    @Override
    public String toString() {
        return "QiyuCurrencyAccountDTO{" +
                "createTime=" + createTime +
                ", userId=" + userId +
                ", currentBalance=" + currentBalance +
                ", totalCharged=" + totalCharged +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(int currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public int getTotalCharged() {
        return totalCharged;
    }

    public void setTotalCharged(int totalCharged) {
        this.totalCharged = totalCharged;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
