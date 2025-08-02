package org.qiyu.live.im.provider.service;

public interface ImOnlineService {
    /**
     * 判断对于业务的userId的主机是否在线
     */
    boolean isOnline(Long userId, int appId);
}
