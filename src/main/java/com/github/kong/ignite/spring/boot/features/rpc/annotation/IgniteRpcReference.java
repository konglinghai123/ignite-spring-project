package com.github.kong.ignite.spring.boot.features.rpc.annotation;

import java.lang.annotation.*;

/**
 * 网格服务注入注解
 */
@Target({
        ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgniteRpcReference {

    String version() default "1.0";

    //默认使用负载均衡
    boolean isLoadbalance() default true;

    //默认不设超时
    long timeout() default 0;
}
