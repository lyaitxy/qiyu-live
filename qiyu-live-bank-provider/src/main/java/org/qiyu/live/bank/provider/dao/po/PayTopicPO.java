package org.qiyu.live.bank.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_pay_topic")
public class PayTopicPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String topic;
    private Integer status;
    private Integer bizCode;
    private String remark;
    private Date createTime;
    private Date updateTime;

    public Integer getBizCode() {
        return bizCode;
    }

    public void setBizCode(Integer bizCode) {
        this.bizCode = bizCode;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
