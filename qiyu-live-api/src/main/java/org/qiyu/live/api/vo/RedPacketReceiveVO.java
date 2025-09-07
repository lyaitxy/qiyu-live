package org.qiyu.live.api.vo;

public class RedPacketReceiveVO {

    private Integer price;
    private String msg;

    @Override
    public String toString() {
        return "RedPacketReceiveVO{" +
                "msg='" + msg + '\'' +
                ", price=" + price +
                '}';
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
