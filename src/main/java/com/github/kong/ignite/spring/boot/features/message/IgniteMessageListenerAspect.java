package com.github.kong.ignite.spring.boot.features.message;

import com.github.kong.ignite.spring.boot.admin.model.ServiceInfo;
import com.github.kong.ignite.spring.boot.features.message.annotation.IgniteMessageListener;
import com.github.kong.ignite.spring.boot.features.message.model.MessageModel;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.Lock;


/**
 * 拦截消息接收，并统计收发情况
 */
@Configuration
@Aspect
public class IgniteMessageListenerAspect {


    private Logger logger = LoggerFactory.getLogger(IgniteMessageListenerAspect.class);

    private final static String CONSUMER_CACHE = "Consumer_Cache";


    @Autowired
    private IgniteMessageStatistical statistical;

    @Autowired
    @Qualifier("igniteClient")
    private Ignite igniteClient;

    @Bean
    public IgniteCache<String, ServiceInfo> messageConsumeCache(@Qualifier("igniteServer") Ignite ignite){
        CacheConfiguration<String, ServiceInfo> cacheConfiguration = new CacheConfiguration<>();
        cacheConfiguration.setName(CONSUMER_CACHE);
        cacheConfiguration.setCacheMode(CacheMode.REPLICATED);
        cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        return ignite.getOrCreateCache(cacheConfiguration);
    }


    @Pointcut("@within(com.github.kong.ignite.spring.boot.features.message.annotation.IgniteMessageListener)")
    public void messageListenerPointCut() {

    }



    @Around("messageListenerPointCut()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {

        IgniteMessageListener messageListener = joinPoint.getTarget().getClass().getAnnotation(IgniteMessageListener.class);

        String topic =  messageListener.topic();

        Boolean isBroadcast = messageListener.isBroadcast();


        Object result = null;

        // 如果是面向集群的全部节点的，那么让消息都发给它们
        if(isBroadcast){
            result = process(joinPoint,topic);
        }else{
            // 使用ignite cache 来实现分布式锁
            Object[] args = joinPoint.getArgs();

            MessageModel messageModel = (MessageModel) args[1];

            String messageId = messageModel.getUuid();

            IgniteCache<String,String> cache = igniteClient.cache(CONSUMER_CACHE);

            Lock lock = cache.lock(messageId);
            try {
                //如果可以拿到锁，那么可以让它执行
                if(lock.tryLock()){
                  result = process(joinPoint,topic);
                }

            }finally {
                lock.unlock();
            }
        }

        return result;
    }


    private Object process(ProceedingJoinPoint joinPoint,String topic) throws Throwable{
        Object result = null;
        try {
            result = joinPoint.proceed();
            statistical.addRecevice(topic);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            statistical.addError(topic);
        }

        return result;
    }

}
