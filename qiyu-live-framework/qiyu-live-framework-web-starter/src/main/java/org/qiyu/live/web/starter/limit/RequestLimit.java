package org.qiyu.live.web.starter.limit;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimit {

    int limit();

    int second();

    String msg() default "请求过于频繁";
}
