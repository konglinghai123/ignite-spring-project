package com.github.kong.ignite.spring.boot.features.rpc.consumer;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterGroup;
import org.dozer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 使用动态代理调用服务网格
 */
public class IgniteRpcConsumerBean implements  Serializable {

    private final static long serialVersionUID = 2596393671406420422L;

    private static final Logger logger = LoggerFactory.getLogger(IgniteRpcConsumerBean.class);

    private Class<?> iface = null;
    private String version = null;
    private String serviceName;
    private Ignite igniteClient;
    private boolean isLoadBlance;
    private long timeout;

    public Class<?> getIface() {
        return iface;
    }

    public void setIface(Class<?> iface) {
        this.iface = iface;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Ignite getIgniteClient() {
        return igniteClient;
    }

    public void setIgniteClient(Ignite igniteClient) {
        this.igniteClient = igniteClient;
    }

    public boolean isLoadBlance() {
        return isLoadBlance;
    }

    public void setLoadBlance(boolean loadBlance) {
        isLoadBlance = loadBlance;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iface}, new IgniteServiceProxy());
    }

    class IgniteServiceProxy implements InvocationHandler,Serializable {

        private final static long serialVersionUID = 2596393671406420411L;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                ClusterGroup clusterGroup = igniteClient.cluster().forRemotes();
                Object serviceProxy;

                String realServiceName = serviceName +"_v"+version;

                if (timeout != 0) {
                    serviceProxy = igniteClient.services(clusterGroup).serviceProxy(realServiceName, iface, !isLoadBlance, timeout);
                } else {
                    serviceProxy = igniteClient.services(clusterGroup).serviceProxy(realServiceName, iface, !isLoadBlance);
                }
                // 使用反射获取 proxyObj.methodName 方法
                Method igniteMethod = ReflectionUtils.getMethod(serviceProxy, method.getName());
                return igniteMethod.invoke(serviceProxy, args);
            } catch (Exception e) {
                logger.error("执行远程方法错误", e);
                throw new RuntimeException("执行远程方法错误",e);
            }
        }
    }
}
