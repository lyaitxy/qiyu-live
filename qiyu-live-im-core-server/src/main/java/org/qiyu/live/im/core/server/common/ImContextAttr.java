package org.qiyu.live.im.core.server.common;

import io.netty.util.AttributeKey;

/**
 * Channel的属性键，保存netty的域信息
 */
public class ImContextAttr {
    /**
     * 绑定用户id
     */
    public static AttributeKey<Long>  USER_ID = AttributeKey.valueOf("userId");
    /**
     * 绑定appId
     */
    public static AttributeKey<Integer> APP_ID = AttributeKey.valueOf("appId");
    /**
     * 绑定roomId
     */
    public static AttributeKey<Integer> ROOM_ID = AttributeKey.valueOf("roomId");
}
