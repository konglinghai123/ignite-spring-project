package com.github.kong.ignite.spring.boot.features.rpc.provider;


import com.github.kong.ignite.spring.boot.features.rpc.IgniteService;
import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import com.github.kong.ignite.spring.boot.features.rpc.annotation.IgniteRpcService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteServices;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.ignite.events.EventType.EVT_NODE_FAILED;
import static org.apache.ignite.events.EventType.EVT_NODE_LEFT;

/**
 * 服务网格提供者Bean注入
 */
public class IgniteRpcServiceProviderFactory
        implements ApplicationContextAware, InitializingBean, DisposableBean, Serializable {

    private final static long serialVersionUID = 2596393671406420400L;

    private Logger logger = LoggerFactory.getLogger(IgniteRpcServiceProviderFactory.class);

    private Ignite igniteServer = null;

    @Value("${ignite-cluster.role}")
    private String role;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {

        logger.info(">>>>>>>>>>> 正在初始化网格RPC提供者...");

        registerDiscoveryEvtListener(igniteServer);

        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(IgniteRpcService.class);
        if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
            for (String beanName : serviceBeanMap.keySet()) {

                Object serviceBean = serviceBeanMap.get(beanName);
                // valid
                if (serviceBean.getClass().getInterfaces().length == 0) {
                    throw new IgniteException("RPC服务必须实现Api接口");
                }

                // spring4 和 spring5 的aop代理不一样，不能直接通过serviceBean.getClass().getAnnotation(IgniteMessageListener.class) 获取

                IgniteRpcService rpcService = applicationContext.findAnnotationOnBean(beanName,IgniteRpcService.class);

                //类名
                String className = StringUtils.substringAfterLast(serviceBean.getClass().getInterfaces()[0].getName(), ".");

                //接口版本号
                String version = rpcService.version();

                // test_v1.1
                String serviceName = className + "_v" + version;

                if (!(serviceBean instanceof IgniteService)) {
                    throw new IgniteException("RPC服务必须实现IgniteService接口");
                }

                IgniteService igniteService = (IgniteService) serviceBean;
                igniteService.setServiceName(serviceName);
                igniteService.setClassName(serviceBean.getClass().getInterfaces()[0].getName());
                igniteService.setDes(rpcService.des());
                igniteService.setTotalCount(rpcService.total());
                igniteService.setMaxPerNodeCount(rpcService.maxPerNodeCount());
                deployService(serviceName, igniteService, rpcService.total(), rpcService.maxPerNodeCount());
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info(">>>>>>>>>>> 初始化网格RPC提供者成功！");
    }

    @Override
    public void destroy() throws Exception {
        igniteServer.close();
    }

    public void setIgniteServer(Ignite igniteServer) {
        this.igniteServer = igniteServer;
    }

    public void deployService(String name, Service service, int totalCount, int maxPerNodeCount) {
        IgniteServices services = igniteServer.services();

        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setName(name);
        serviceConfiguration.setService(service);
        serviceConfiguration.setNodeFilter((IgnitePredicate<ClusterNode>) (ClusterNode clusterNode) -> StringUtils.equals(role, clusterNode.attribute("role"))&& !clusterNode.isClient());
        serviceConfiguration.setTotalCount(totalCount);
        serviceConfiguration.setMaxPerNodeCount(maxPerNodeCount);
        services.deploy(serviceConfiguration);
    }

    /**
     * 监听节点离开事件，实时更新服务缓存信息
     */
    public void registerDiscoveryEvtListener(Ignite igniteServer) {

        igniteServer.events().localListen(new IgnitePredicate<DiscoveryEvent>() {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean apply(DiscoveryEvent e) {

                String original = e.eventNode().id().toString();

                List<ServiceInfo> updateInfos = new ArrayList<>();
                IgniteCache<String,ServiceInfo> igniteCache = igniteServer.cache(ServiceInfo.SERVICE_CACHE);
                igniteCache.forEach(objectObjectEntry -> {
                    ServiceInfo serviceInfo =  objectObjectEntry.getValue();
                    if(serviceInfo.getNodeId().equals(original)){
                        serviceInfo.setStatus(0);
                        updateInfos.add(serviceInfo);
                    }
                });

                updateInfos.forEach(serviceInfo -> igniteServer.cache(ServiceInfo.SERVICE_CACHE).put(serviceInfo.getServiceName(),serviceInfo));

                return true;
            }
        }, EVT_NODE_FAILED, EVT_NODE_LEFT);

    }

}
