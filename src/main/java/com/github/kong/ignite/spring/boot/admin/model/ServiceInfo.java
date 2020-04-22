package com.github.kong.ignite.spring.boot.admin.model;

import java.io.Serializable;

/**
 * 服务网格中部署的微服务的基本信息
 */
public class ServiceInfo implements Serializable {

    public final static String SERVICE_CACHE = "serviceInfo";
    private String serviceName;
    private String className;
    private Integer totalCount;
    private Integer maxPerNodeCount;
    private String nodeId;
    private String des;
    //1:可用 0:不可用
    private Integer status = 0;

    public ServiceInfo() {

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

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getMaxPerNodeCount() {
        return maxPerNodeCount;
    }

    public void setMaxPerNodeCount(Integer maxPerNodeCount) {
        this.maxPerNodeCount = maxPerNodeCount;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
