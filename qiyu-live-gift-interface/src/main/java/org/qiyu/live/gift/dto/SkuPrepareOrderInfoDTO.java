package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class SkuPrepareOrderInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8683020132073931910L;

    private Integer totalPrice;
    private List<ShopCarItemRespDTO> skuPrepareOrderItemInfoDTOS;

    @Override
    public String toString() {
        return "SkuPrepareOrderInfoDTO{" +
                "skuPrepareOrderItemInfoDTOS=" + skuPrepareOrderItemInfoDTOS +
                ", totalPrice=" + totalPrice +
                '}';
    }

    public List<ShopCarItemRespDTO> getSkuPrepareOrderItemInfoDTOS() {
        return skuPrepareOrderItemInfoDTOS;
    }

    public void setSkuPrepareOrderItemInfoDTOS(List<ShopCarItemRespDTO> skuPrepareOrderItemInfoDTOS) {
        this.skuPrepareOrderItemInfoDTOS = skuPrepareOrderItemInfoDTOS;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }
}
