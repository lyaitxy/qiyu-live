package org.qiyu.live.api.error;

import org.qiyu.live.web.starter.error.QiyuBaseError;

public enum ApiErrorEnum implements QiyuBaseError {

    LIVING_ROOM_TYPE_MISSING(1, "需要给定直播间类型"),
    PHONE_NOT_BLANK(2, "手机号不能为空"),
    PHONE_IN_VALID(3, "手机号格式异常"),
    LOGIN_CODE_IN_VALID(4, "验证码格式异常"),
    GIFT_CONFIG_ERROR(5, "礼物信息异常"),
    SEND_GIFT_ERROR(6, "礼物信息异常"),
    PK_ONLINE_BUSY(7, "当前正有人连线，请稍后再试"),
    NOT_SEND_TO_YOURSELF(8, "目前正有人连线，不能给自己送礼");

    ApiErrorEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    int code;
    String desc;

    @Override
    public int getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMsg() {
        return desc;
    }
}
