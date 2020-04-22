package com.github.kong.ignite.spring.boot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 服务网格配置
 */
@ConfigurationProperties(prefix = "ignite-cluster")
public class IgniteClusterProperties {

    //节点地址
    private String localAddress;

    //节点发现端口
    private Integer localPort;

    //集群角色名称
    public String name;

    //集成角色
    public String role;

    //描述
    public String des;

    // zookeeper 地址
    public String zookeeperUrl;

    // 组播地址，不使用静态ip地址发现
    public String multicastGroup;

    //权重
    public Integer nodeWeight = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getZookeeperUrl() {
        return zookeeperUrl;
    }

    public void setZookeeperUrl(String zookeeperUrl) {
        this.zookeeperUrl = zookeeperUrl;
    }

    public String getMulticastGroup() {
        return multicastGroup;
    }

    public void setMulticastGroup(String multicastGroup) {
        this.multicastGroup = multicastGroup;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public Integer getNodeWeight() {
        return nodeWeight;
    }

    public void setNodeWeight(Integer nodeWeight) {
        this.nodeWeight = nodeWeight;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }


    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }
}
