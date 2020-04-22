package com.github.kong.ignite.spring.boot.features.rpc.config;

import com.github.kong.ignite.spring.boot.features.rpc.consumer.impl.IgniteRpcSpringClientFactory;
import org.apache.ignite.Ignite;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务网格调用者的初始化
 */
@Configuration
public class IgniteRpcClientConfig {


    @Bean
    public IgniteRpcSpringClientFactory igniteRpc(@Qualifier("igniteClient") Ignite igniteClient) {

        IgniteRpcSpringClientFactory igniteRpcSpringClientFactory = new IgniteRpcSpringClientFactory();
        igniteRpcSpringClientFactory.setIgniteClient(igniteClient);
        return igniteRpcSpringClientFactory;
    }


}
