package org.qiyu.live.living.constants;

/**
 * 直播间的类型枚举
 */
public enum LivingRoomTypeEnum {
    DEFAULT_LIVING_ROOM(1, "普通直播间"),
    PK_LIVING_ROOM(2, "pk直播间");

    Integer code;
    String desc;

    LivingRoomTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getCode() {
        return code;
    }
}
