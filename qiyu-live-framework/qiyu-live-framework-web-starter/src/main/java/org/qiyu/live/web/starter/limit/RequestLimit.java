package org.qiyu.live.web.starter.limit;

import org.qiyu.live.web.starter.constants.LimitStrategy;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimit {

    int limit();

    int second();

    String msg() default "请求过于频繁";

    LimitStrategy strategy() default LimitStrategy.FIXED_WINDOW;

    // 以下是令牌桶的参数
    int capacity() default -1;
}
