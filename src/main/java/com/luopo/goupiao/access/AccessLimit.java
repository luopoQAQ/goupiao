package com.luopo.goupiao.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit { //访问限制注解（用法一：只设置seconds与maxcount为同时限制访问次数与登录）
                                //          （用法二：没有注解值为只要求登录，不限制访问）
                                //          （只建议这两种用法）
                                //解释：访问限流（限定seconds秒内只允许访问maxCount次，通过redis实现）
                                //     注解必须登录（因为后面是针对同一url下同一用户做的限制）
    int seconds() default -1;

    int maxCount() default -1;

    boolean needLogin() default true;   //必须登录

}
