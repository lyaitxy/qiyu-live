package org.qiyu.live.framework.redis.starter.key;

import org.springframework.beans.factory.annotation.Value;

public class RedisKeyBuilder {

    @Value("${spring.application.name}")
    private String applicationName;
    private static final String SPLIT_ITEM = ":";

    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    /**
     * 前缀就是模块名加冒号
     * @return
     */
    public String getPrefix() {
        return applicationName + SPLIT_ITEM;
    }

}
