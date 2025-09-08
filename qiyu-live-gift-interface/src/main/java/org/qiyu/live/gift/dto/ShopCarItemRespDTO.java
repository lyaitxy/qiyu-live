package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;

public class ShopCarItemRespDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7247175817439564893L;

    private Integer count;
    private SkuInfoDTO skuInfoDTO;

    @Override
    public String toString() {
        return "ShopCarItemRespDTO{" +
                "count=" + count +
                ", skuInfoDTO=" + skuInfoDTO +
                '}';
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public SkuInfoDTO getSkuInfoDTO() {
        return skuInfoDTO;
    }

    public void setSkuInfoDTO(SkuInfoDTO skuInfoDTO) {
        this.skuInfoDTO = skuInfoDTO;
    }
}
