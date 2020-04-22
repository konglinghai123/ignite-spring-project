package com.github.kong.ignite.spring.boot.features.mr;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * map-reduce 执行器
 * @param <T>
 * @param <R>
 */
@Service
public class MapReduceTaskExecutor<T, R> {

    @Autowired
    private Ignite igniteClient;

    @Value("${ignite-cluster.role}")
    private String role;

    /**
     *  默认采取与当前相同角色的集群组执行MapReduce任务
     * @param taskClass
     * @param arg
     * @return
     */
    public R execute(Class<? extends ComputeTaskSplitAdapter<T, R>> taskClass, T arg){
        //限定MapReduce执行范围，必须是同一个角色类型
        ClusterGroup clusterGroup = igniteClient.cluster().forAttribute("role",role);
        return igniteClient.compute(clusterGroup).execute(taskClass, arg);
    }

    /**
     *  指定集群组执行MapReduce任务
     * @param taskClass
     * @param arg
     * @return
     */
    public R execute(Class<? extends ComputeTaskSplitAdapter<T, R>> taskClass, T arg,String targetRole){
        //限定MapReduce执行范围，必须是同一个角色类型
        ClusterGroup clusterGroup = igniteClient.cluster().forAttribute("role",targetRole);
        return igniteClient.compute(clusterGroup).execute(taskClass, arg);
    }

}
