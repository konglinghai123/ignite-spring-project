package com.github.kong.ignite.spring.boot.features.message.annotation;

import java.lang.annotation.*;

/**
 * 服务提供者注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgniteMessageListener {


    //消息主题
    String topic();

    //消息描述
    String des() default "";

    //是否针对集群内的所有节点
    boolean isBroadcast() default true;

}
