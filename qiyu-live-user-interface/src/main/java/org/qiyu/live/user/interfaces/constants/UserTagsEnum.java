package org.qiyu.live.user.interfaces.constants;

public enum UserTagsEnum {

    IS_RICH((long)Math.pow(2, 0), "是否是有钱用户", "tag_info_01"),
    IS_VIP((long)Math.pow(2, 1), "是否是VIP用户", "tag_info_01"),
    IS_OLD_USER((long)Math.pow(2, 2), "是否是老用户", "tag_info_01");

    private long tag;
    private String desc;
    private String fieldName;

    UserTagsEnum(long tag, String desc, String fieldName) {
        this.desc = desc;
        this.fieldName = fieldName;
        this.tag = tag;
    }

    public String getDesc() {
        return desc;
    }

    public String getFieldName() {
        return fieldName;
    }

    public long getTag() {
        return tag;
    }
}
