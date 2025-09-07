package org.qiyu.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;

public class RedPacketReceiveDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5916608127876611063L;

    private Integer price;
    private String notifyMsg;
    private boolean status;

    public String getNotifyMsg() {
        return notifyMsg;
    }

    public void setNotifyMsg(String notifyMsg) {
        this.notifyMsg = notifyMsg;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public RedPacketReceiveDTO(String notifyMsg, Integer price, boolean status) {
        this.notifyMsg = notifyMsg;
        this.price = price;
        this.status = status;
    }
}
