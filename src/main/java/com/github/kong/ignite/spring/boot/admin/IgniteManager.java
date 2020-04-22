package com.github.kong.ignite.spring.boot.admin;


import com.github.kong.ignite.spring.boot.admin.model.MessageInfo;
import com.github.kong.ignite.spring.boot.admin.model.NodeInfo;
import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import org.apache.ignite.cluster.ClusterMetrics;

import java.util.List;

public interface IgniteManager {

    /**
     * 获取节点列表
     *
     * @return
     */
    List<NodeInfo> list();

    /**
     * 获取节点的详细信息
     *
     * @param nodeId
     * @return
     */
    ClusterMetrics info(String nodeId);

    /**
     * 获取微服务的基本信息
     *
     * @return
     */
    List<ServiceInfo> servieInfos();

    /**
     * 集群消息信息
     * @return
     */
    List<MessageInfo> messagInfos();

}
