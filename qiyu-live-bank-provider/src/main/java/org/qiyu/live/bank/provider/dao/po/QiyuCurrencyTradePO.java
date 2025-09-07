package org.qiyu.live.bank.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 虚拟货币账户
 */
@TableName("t_qiyu_currency_trade")
public class QiyuCurrencyTradePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer num;
    private Integer type;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    @Override
    public String toString() {
        return "QiyuCurrencyTradePO{" +
                "createTime=" + createTime +
                ", id=" + id +
                ", userId=" + userId +
                ", num=" + num +
                ", type=" + type +
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
