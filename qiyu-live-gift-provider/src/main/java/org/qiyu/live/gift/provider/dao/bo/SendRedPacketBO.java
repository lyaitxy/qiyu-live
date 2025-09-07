package org.qiyu.live.gift.provider.dao.bo;

import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;

import java.io.Serial;
import java.io.Serializable;

public class SendRedPacketBO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1829802295999336708L;

    private Integer price;
    private RedPacketConfigReqDTO reqDTO;

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public RedPacketConfigReqDTO getReqDTO() {
        return reqDTO;
    }

    public void setReqDTO(RedPacketConfigReqDTO reqDTO) {
        this.reqDTO = reqDTO;
    }
}
