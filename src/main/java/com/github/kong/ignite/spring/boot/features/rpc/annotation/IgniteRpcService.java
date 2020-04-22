package com.github.kong.ignite.spring.boot.features.rpc.annotation;

import java.lang.annotation.*;

/**
 * 服务提供者注解
 */
@Target({
        ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgniteRpcService {

    /**
     * @return
     */
    String version() default "1.0";

    //接口描述
    String des() default "";

    //单个节点部署的实例数
    int maxPerNodeCount() default 1;

    //整个集群部署的最大实例数，0：无限制
    int total() default 0;

}
