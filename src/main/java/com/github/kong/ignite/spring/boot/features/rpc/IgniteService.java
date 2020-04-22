package com.github.kong.ignite.spring.boot.features.rpc;

import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象的一层网格服务，避免重复实现原生 ignite 的 service 接口
 */
public abstract class IgniteService implements Service {

    private Logger logger = LoggerFactory.getLogger(IgniteService.class);

    private String serviceName;

    private String className;

    private String des;

    private Integer maxPerNodeCount;

    private Integer totalCount;

    @IgniteInstanceResource
    private transient Ignite ignite;

    public String localNodeId() {
        return ignite.cluster().forLocal().node().id().toString();
    }

    @Override
    public void cancel(ServiceContext ctx) {
        logger.info("服务【 " + getServiceName() + "】已经取消部署");
    }

    @Override
    public void init(ServiceContext ctx) throws Exception {
    }

    @Override
    public void execute(ServiceContext ctx) throws Exception {

        String nodeId = localNodeId();
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setNodeId(nodeId);
        serviceInfo.setClassName(className);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setTotalCount(totalCount);
        serviceInfo.setMaxPerNodeCount(maxPerNodeCount);
        serviceInfo.setDes(des);
        serviceInfo.setStatus(1);

        //缓存记录注册服务
        ignite.cache(ServiceInfo.SERVICE_CACHE).put(serviceName,serviceInfo);

        logger.info("节点【" + nodeId + "】 【 " + getServiceName() + " 】服务已经部署完毕");
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public Integer getMaxPerNodeCount() {
        return maxPerNodeCount;
    }

    public void setMaxPerNodeCount(Integer maxPerNodeCount) {
        this.maxPerNodeCount = maxPerNodeCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
