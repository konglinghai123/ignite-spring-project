package com.github.kong.ignite.spring.boot.admin.impl;


import com.github.kong.ignite.spring.boot.admin.IgniteManager;
import com.github.kong.ignite.spring.boot.admin.model.MessageInfo;
import com.github.kong.ignite.spring.boot.admin.model.NodeInfo;
import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.cluster.ClusterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 服务网格管理，可以查询网格集群中已经部署的服务
 */
@Component
public class IgniteManagerImpl implements IgniteManager {

    @Autowired
    @Qualifier("igniteClient")
    private Ignite igniteClient;

    @Override
    public List<NodeInfo> list() {
        List<NodeInfo> nodeInfos = new ArrayList<>();
        IgniteCluster cluster = igniteClient.cluster();
        //因为每个节点都是单独的服务，所以以服务为标志
        for (ClusterNode node : cluster.forServers().nodes()) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setId(node.id().toString());
            nodeInfo.setName(node.attribute("name"));
            nodeInfo.setRole(node.attribute("role"));
            nodeInfo.setStartTime(node.attribute("startTime"));
            nodeInfo.setDes(node.attribute("des"));
            nodeInfo.setHostName(node.hostNames());
            nodeInfos.add(nodeInfo);
        }
        return nodeInfos;
    }

    @Override
    public ClusterMetrics info(String nodeId) {
        IgniteCluster cluster = igniteClient.cluster();
        UUID uuid = UUID.fromString(nodeId);
        ClusterNode node = cluster.node(uuid);
        return node.metrics();
    }

    @Override
    public List<ServiceInfo> servieInfos() {
        List<ServiceInfo> serviceInfos = new ArrayList<>();

        igniteClient.cache(ServiceInfo.SERVICE_CACHE).forEach(objectObjectEntry -> {
            ServiceInfo serviceInfo = (ServiceInfo) objectObjectEntry.getValue();
            serviceInfos.add(serviceInfo);
        });

        return serviceInfos;
    }

    @Override
    public List<MessageInfo> messagInfos() {
        List<MessageInfo> messageInfos = new ArrayList<>();

        igniteClient.cache(MessageInfo.MESSAGE_CACHE).forEach(objectObjectEntry -> {
            MessageInfo serviceInfo = (MessageInfo) objectObjectEntry.getValue();
            messageInfos.add(serviceInfo);
        });

        return messageInfos;
    }
}
