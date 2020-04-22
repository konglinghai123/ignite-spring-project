package com.github.kong.ignite.spring.boot.features.rpc.consumer.impl;

import com.github.kong.ignite.spring.boot.features.rpc.annotation.IgniteRpcReference;
import com.github.kong.ignite.spring.boot.features.rpc.consumer.IgniteRpcConsumerBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;

/**
 * IgniteRpcReference 注解的处理
 */
public class IgniteRpcSpringClientFactory extends InstantiationAwareBeanPostProcessorAdapter
        implements InitializingBean, DisposableBean, BeanFactoryAware {
    private Logger logger = LoggerFactory.getLogger(IgniteRpcSpringClientFactory.class);

    private Ignite igniteClient;

    public void setIgniteClient(Ignite igniteClient) {
        this.igniteClient = igniteClient;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        logger.info(">>>>>>>>>>> 正在初始化网格RPC客户端");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info(">>>>>>>>>>> 初始化网格RPC客户端成功！");
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName)
            throws BeansException {

        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (field.isAnnotationPresent(IgniteRpcReference.class)) {

                Class iface = field.getType();
                if (!iface.isInterface()) {
                    throw new IgniteException("RPC服务必须实现一个接口");
                }

                IgniteRpcReference rpcReference = field.getAnnotation(IgniteRpcReference.class);

                String serviceName = StringUtils.substringAfterLast(field.getType().getName(), ".");

                IgniteRpcConsumerBean referenceBean = new IgniteRpcConsumerBean();
                referenceBean.setServiceName(serviceName);
                referenceBean.setIface(field.getType());
                referenceBean.setIgniteClient(igniteClient);
                referenceBean.setVersion(rpcReference.version());
                referenceBean.setLoadBlance(rpcReference.isLoadbalance());
                referenceBean.setTimeout(rpcReference.timeout());

                try {
                    Object serviceProxy = referenceBean.getObject();
                    field.setAccessible(true);
                    field.set(bean, serviceProxy);
                } catch (Exception e) {
                    logger.info(">>>>>>>>>>> 初始化网格RPC客户端失败！", e);
                    throw new RuntimeException(e);
                }
            }
        });

        return super.postProcessAfterInstantiation(bean, beanName);
    }

    @Override
    public void destroy() throws Exception {
        igniteClient.close();
    }

}
