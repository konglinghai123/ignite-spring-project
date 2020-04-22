package com.github.kong.ignite.spring.boot;

import com.github.kong.ignite.spring.boot.configuration.IgniteClusterProperties;
import com.github.kong.ignite.spring.boot.extend.IgniteNoopCheckpointSpi;
import com.github.kong.ignite.spring.boot.extend.TcpCommunicationSpi;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.FileSystemConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.failure.StopNodeFailureHandler;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.zk.TcpDiscoveryZookeeperIpFinder;
import org.apache.ignite.spi.loadbalancing.weightedrandom.WeightedRandomLoadBalancingSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;


/**
 * ignite 集群配置
 */
@Configuration
@EnableConfigurationProperties(IgniteClusterProperties.class)
@ComponentScan("com.github.kong.*")
public class IgniteClusterAutoConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public IgniteClusterProperties clusterProperties;

    private void activeCluster(Ignite ignite) {
        if (!ignite.cluster().active()) {
            ignite.cluster().active(true);  //如果集群未启动则启动集群
        }
    }

    private IgniteConfiguration serverConfiguration() {
        TcpDiscoverySpi spi = tcpDiscoverySpi();
        return igniteConfiguration(spi, false);
    }

    private IgniteConfiguration clientConfiguration() {
        TcpDiscoverySpi spi = tcpDiscoverySpi();
        spi.failureDetectionTimeoutEnabled(true);
        return igniteConfiguration(spi, true);
    }

    private TcpDiscoverySpi tcpDiscoverySpi() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
//        spi.setLocalAddress(clusterProperties.getLocalAddress());
        //优先使用zookeeper注册
        if (StringUtils.isNotBlank(clusterProperties.getZookeeperUrl())) {
            TcpDiscoveryZookeeperIpFinder zookeeperIpFinder = new TcpDiscoveryZookeeperIpFinder();
            zookeeperIpFinder.setAllowDuplicateRegistrations(true);
            zookeeperIpFinder.setZkConnectionString(clusterProperties.getZookeeperUrl());
            spi.setIpFinder(zookeeperIpFinder);
        }else{
            TcpDiscoveryMulticastIpFinder tcMp = new TcpDiscoveryMulticastIpFinder();
            tcMp.setMulticastGroup(clusterProperties.getMulticastGroup());
            spi.setIpFinder(tcMp);
        }

        return spi;
    }

    //负载均衡配置
    private WeightedRandomLoadBalancingSpi weightedRandomLoadBalancingSpi() {
        WeightedRandomLoadBalancingSpi weightedRandomLoadBalancingSpi = new WeightedRandomLoadBalancingSpi();
        weightedRandomLoadBalancingSpi.setNodeWeight(clusterProperties.getNodeWeight());
        weightedRandomLoadBalancingSpi.setUseWeights(true);
        return weightedRandomLoadBalancingSpi;
    }

    private IgniteConfiguration igniteConfiguration(DiscoverySpi spi, Boolean isClient) {

        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setIgniteHome(getHome() + "/" + clusterProperties.getName());
        cfg.setWorkDirectory(getHome() + "/" + clusterProperties.getName()+"/work");


        cfg.setCheckpointSpi(new IgniteNoopCheckpointSpi());

        // 集群故障检测
        TcpCommunicationSpi tcpCommunicationSpi = new TcpCommunicationSpi();
        tcpCommunicationSpi.setMessageQueueLimit(250);
        tcpCommunicationSpi.setSlowClientQueueLimit(125);
        tcpCommunicationSpi.setLocalPortRange(10);
        tcpCommunicationSpi.setLocalAddress(clusterProperties.getLocalAddress());
        tcpCommunicationSpi.setLocalPort(clusterProperties.getLocalPort());

        cfg.setCommunicationSpi(tcpCommunicationSpi);
        cfg.setCacheSanityCheckEnabled(false);
        cfg.setFailureHandler(new StopNodeFailureHandler());
        cfg.setConsistentId(clusterProperties.getName());
        cfg.setClientMode(isClient);
        cfg.setMetricsLogFrequency(0);

        // ignite 用户参数
        Map<String, Object> userConfig = new HashMap();
        userConfig.put("role", clusterProperties.getRole());
        userConfig.put("name", clusterProperties.getName());
        userConfig.put("des", clusterProperties.getDes());
        userConfig.put("startTime", String.valueOf(System.currentTimeMillis()));
        cfg.setUserAttributes(userConfig);

        cfg.setGridLogger(new Slf4jLogger(logger));


        // ignite实例名称
        if (isClient) {
            cfg.setIgniteInstanceName(clusterProperties.getName() + "_client");
        } else {
            cfg.setIgniteInstanceName(clusterProperties.getName() + "_server");
            /**
             *  ignite集群间字节码交换，手动写的代码只发送到一个点，其他节点动态感知，这一块可能有问题实际使用时还是每个节点都发布代码为好
             *  生产环境中禁用对等类加载以免影响性能
             */
            cfg.setPeerClassLoadingEnabled(false);
            // igfs 文件系统
            cfg.setFileSystemConfiguration(igfsConfiguration());
        }

        //负载均衡
        cfg.setLoadBalancingSpi(weightedRandomLoadBalancingSpi());
        cfg.setDiscoverySpi(spi);

        //慢作业调度
//        cfg.setCollisionSpi(new JobStealingCollisionSpi());

        //数据再平衡线程
        cfg.setRebalanceThreadPoolSize(4);

        return cfg;
    }

    private static String getHome() {
        return System.getProperty("user.dir") + "/ignite";
    }

    /**
     * igfs 默认配置
     * @return
     */
    private FileSystemConfiguration igfsConfiguration(){
        FileSystemConfiguration configuration = new FileSystemConfiguration();
        configuration.setBlockSize(128 * 1024);
        configuration.setName("igfs");
        configuration.setPerNodeBatchSize(512);
        configuration.setPerNodeParallelBatchCount(16);
        configuration.setPrefetchBlocks(32);
        return configuration;
    }

    @Bean(name = "igniteServer")
    public Ignite igniteServer() {
        System.setProperty("java.net.preferIPv4Stack","true");
        Ignite ignite = Ignition.start(serverConfiguration());
        activeCluster(ignite);
        return ignite;
    }

    @Bean(name = "igniteClient")
    @DependsOn("igniteServer")
    public Ignite igniteClient() {
        Ignite ignite = Ignition.start(clientConfiguration());
        activeCluster(ignite);
        return ignite;
    }



}
