package org.qiyu.live.common.interfaces.enums;

public enum CommonStatusEnum {
    VALID_STATUS(1, "有效"),
    INVALID_STATUS(1, "无效");

    int code;
    String desc;

    CommonStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
