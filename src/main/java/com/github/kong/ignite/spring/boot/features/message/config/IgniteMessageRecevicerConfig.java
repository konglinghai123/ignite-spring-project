package com.github.kong.ignite.spring.boot.features.message.config;

import com.github.kong.ignite.spring.boot.admin.model.MessageInfo;
import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import com.github.kong.ignite.spring.boot.features.message.IgniteMessageRecevicerFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 网格消息监听者初始化配置
 */
@Configuration
@DependsOn("igniteServer")
public class IgniteMessageRecevicerConfig {

    @Value("${ignite-cluster.role}")
    private String role;

    @Bean
    public IgniteCache<String, ServiceInfo> messageInfoCache(@Qualifier("igniteServer") Ignite ignite){
        CacheConfiguration<String, ServiceInfo> cacheConfiguration = new CacheConfiguration<>();
        cacheConfiguration.setName(MessageInfo.MESSAGE_CACHE);
        cacheConfiguration.setCacheMode(CacheMode.REPLICATED);
        cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        return ignite.getOrCreateCache(cacheConfiguration);
    }


    @Bean
    public IgniteMessageRecevicerFactory igniteMessageRecevicerFactory(@Qualifier("igniteServer") Ignite igniteServer, @Qualifier("igniteClient") Ignite igniteClient) {
        IgniteMessageRecevicerFactory recevicerFactory = new IgniteMessageRecevicerFactory();
        recevicerFactory.setIgniteServer(igniteServer);
        recevicerFactory.setIgniteClient(igniteClient);
        recevicerFactory.setRole(role);
        return recevicerFactory;
    }

}