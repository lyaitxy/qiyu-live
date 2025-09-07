package org.qiyu.live.api.vo.req;

public class PayProductReqVO {

    // 产品id
    private Integer productId;
    /**
     * 支付来源（直播间内，用户中心），用于统计支付页面来源
     * @see org.qiyu.live.bank.constants.PaySourceEnum
     */
    private Integer paySource;
    /**
     * 支付渠道
     * @see org.qiyu.live.bank.constants.PayChannelEnum
     */
    private Integer payChannel;

    public Integer getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(Integer payChannel) {
        this.payChannel = payChannel;
    }

    public Integer getPaySource() {
        return paySource;
    }

    public void setPaySource(Integer paySource) {
        this.paySource = paySource;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
