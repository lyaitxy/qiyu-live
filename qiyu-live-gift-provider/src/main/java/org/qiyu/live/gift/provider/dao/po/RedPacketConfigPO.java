package org.qiyu.live.gift.provider.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_red_packet_config")
public class RedPacketConfigPO {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long anchorId;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 红包雨结束后，一共领取数量红包
     */
    private Integer totalGet;
    /**
     * 一共领取金额
     */
    private Integer totalGetPrice;
    /**
     * 最大领取金额
     */
    private Integer maxGetPrice;
    private Integer status;
    /**
     * 红包雨总金额数
     */
    private Integer totalPrice;
    /**
     * 红包雨总红包数
     */
    private Integer totalCount;
    private String configCode;
    private String remark;
    private Date createTime;
    private Date updateTime;

    public Long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(Long anchorId) {
        this.anchorId = anchorId;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMaxGetPrice() {
        return maxGetPrice;
    }

    public void setMaxGetPrice(Integer maxGetPrice) {
        this.maxGetPrice = maxGetPrice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
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

    public Integer getTotalGet() {
        return totalGet;
    }

    public void setTotalGet(Integer totalGet) {
        this.totalGet = totalGet;
    }

    public Integer getTotalGetPrice() {
        return totalGetPrice;
    }

    public void setTotalGetPrice(Integer totalGetPrice) {
        this.totalGetPrice = totalGetPrice;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
