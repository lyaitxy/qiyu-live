package org.qiyu.live.api.vo.req;

public class SkuInfoReqVO {

    private Long skuId;
    private Long anchorId;

    @Override
    public String toString() {
        return "SkuInfoReqVO{" +
                "anchorId=" + anchorId +
                ", skuId=" + skuId +
                '}';
    }

    public Long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(Long anchorId) {
        this.anchorId = anchorId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
