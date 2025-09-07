package org.qiyu.live.bank.constants;

public enum TradeTypeEnum {
    SEND_GIFT_TRADE(0, "送礼物交易"),
    LIVING_RECHARGE(1, "直播间充值");

    int code;
    String msg;

    TradeTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
