package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;

public class RedPacketConfigRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5117539613836783248L;
    private Long anchorId;
    private Integer totalPrice;
    private Integer totalCount;
    private String configCode;
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }
}
