package org.qiyu.live.user.interfaces.utils;

public class TagInfoUtils {

    public static boolean isContain(Long tagInfo, Long matchTag) {
        return tagInfo != null && matchTag != null && (tagInfo & matchTag) == matchTag;
    }

    public static void main(String[] args) {
        boolean contain = TagInfoUtils.isContain(4L, 2L);
        System.out.println(contain);
    }
}
