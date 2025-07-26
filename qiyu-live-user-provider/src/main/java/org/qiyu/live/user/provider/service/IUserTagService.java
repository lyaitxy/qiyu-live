package org.qiyu.live.user.provider.service;

import org.qiyu.live.user.interfaces.constants.UserTagsEnum;

public interface IUserTagService {

    /**
     * 设置标签
     */
    boolean setTag(Long userId, UserTagsEnum userTagsEnum);
    /**
     * 取消标签
     */
    boolean cancelTag(Long userId, UserTagsEnum userTagsEnum);
    /**
     * 查看是否含有某标签
     */
    boolean containTag(Long userId, UserTagsEnum userTagsEnum);
}
