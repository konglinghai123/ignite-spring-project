package com.github.kong.ignite.spring.boot.features.broadcast;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * 广播执行器
 */
@Service
public class BroadcastServiceExecutor {

    @Autowired
    private Ignite igniteClient;

    /**
     * 向其他集群广播
     * @param targetRole 集群标识
     * @param targetClass api类
     * @param methodName 方法名称
     * @param args 参数
     * @return
     */
    public List broadcast(String targetRole,Class targetClass,String methodName,Object... args){
        ClusterGroup clusterGroup = igniteClient.cluster().forRemotes().forServers().forAttribute("role",targetRole);
        IgniteBroadcastProxy igniteBroadcastProxy =  new IgniteBroadcastProxy();
        igniteBroadcastProxy.setArgs(args);
        igniteBroadcastProxy.setMethodName(methodName);
        igniteBroadcastProxy.setTargetClass(targetClass);
        return new ArrayList(igniteClient.compute(clusterGroup).broadcast(igniteBroadcastProxy));
    }


    /**
     * 向远端集群广播消息
     * @param targetClass
     * @param methodName
     * @param args
     * @return
     */
    public List broadcastRemote(Class targetClass,String methodName,Object... args){
        ClusterGroup clusterGroup = igniteClient.cluster().forRemotes().forServers();
        IgniteBroadcastProxy igniteBroadcastProxy =  new IgniteBroadcastProxy();
        igniteBroadcastProxy.setArgs(args);
        igniteBroadcastProxy.setMethodName(methodName);
        igniteBroadcastProxy.setTargetClass(targetClass);
        return new ArrayList(igniteClient.compute(clusterGroup).broadcast(igniteBroadcastProxy));
    }

    /**
     * 向集群内广播消息
     * @param targetClass
     * @param methodName
     * @param args
     * @return
     */
    public List broadcastLocal(Class targetClass,String methodName,Object... args){
        ClusterGroup clusterGroup = igniteClient.cluster().forLocal().forServers();
        IgniteBroadcastProxy igniteBroadcastProxy =  new IgniteBroadcastProxy();
        igniteBroadcastProxy.setArgs(args);
        igniteBroadcastProxy.setMethodName(methodName);
        igniteBroadcastProxy.setTargetClass(targetClass);
        return new ArrayList(igniteClient.compute(clusterGroup).broadcast(igniteBroadcastProxy));
    }
}
