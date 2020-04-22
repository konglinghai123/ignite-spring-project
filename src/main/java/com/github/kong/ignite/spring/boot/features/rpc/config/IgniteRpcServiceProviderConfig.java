package com.github.kong.ignite.spring.boot.features.rpc.config;

import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import com.github.kong.ignite.spring.boot.features.rpc.provider.IgniteRpcServiceProviderFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网格服务提供者初始化配置
 */
@Configuration
public class IgniteRpcServiceProviderConfig {

    @Bean
    public IgniteCache<String, ServiceInfo> createAdminCache(@Qualifier("igniteServer") Ignite ignite){
        CacheConfiguration<String, ServiceInfo> cacheConfiguration = new CacheConfiguration<>();
        cacheConfiguration.setName(ServiceInfo.SERVICE_CACHE);
        return ignite.getOrCreateCache(cacheConfiguration);
    }

    @Bean
    public IgniteRpcServiceProviderFactory igniteRpcSpringProviderFactory(@Qualifier("igniteServer") Ignite igniteServer) {
        IgniteRpcServiceProviderFactory providerFactory = new IgniteRpcServiceProviderFactory();
        providerFactory.setIgniteServer(igniteServer);
        return providerFactory;
    }

}