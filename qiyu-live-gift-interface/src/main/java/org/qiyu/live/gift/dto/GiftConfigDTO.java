package org.qiyu.live.gift.dto;

import java.io.Serializable;
import java.util.Date;

public class GiftConfigDTO implements Serializable {

    private Integer giftId;
    /**
     * 虚拟货币价格
     */
    private Integer price;
    /**
     * 礼物名称
     */
    private String giftName;
    /**
     * 状态(0无效， 1有效)
     */
    private Integer status;
    /**
     * 礼物封面地址
     */
    private String coverImgUrl;
    /**
     * svga资源地址
     */
    private String svgaUrl;
    private Date createTime;
    private Date updateTime;

    @Override
    public String toString() {
        return "GiftConfigDTO{" +
                "coverImgUrl='" + coverImgUrl + '\'' +
                ", giftId=" + giftId +
                ", price=" + price +
                ", giftName='" + giftName + '\'' +
                ", status=" + status +
                ", svgaUrl='" + svgaUrl + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSvgaUrl() {
        return svgaUrl;
    }

    public void setSvgaUrl(String svgaUrl) {
        this.svgaUrl = svgaUrl;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
