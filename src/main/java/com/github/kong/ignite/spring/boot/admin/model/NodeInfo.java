package com.github.kong.ignite.spring.boot.admin.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * 服务网格中节点的基本信息
 */
public class NodeInfo implements Serializable {
    private String name;
    private String des;
    private String id;
    private String role;
    private Collection<String> hostName;
    private String startTime;

    public NodeInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Collection<String> getHostName() {
        return hostName;
    }

    public void setHostName(Collection<String> hostName) {
        this.hostName = hostName;
    }
}
