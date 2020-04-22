package com.github.kong.ignite.spring.boot.features.message;


import com.github.kong.ignite.spring.boot.admin.model.MessageInfo;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;


/**
 * 消息调用统计
 */
@Service
public class IgniteMessageStatistical {

    @Autowired
    @Qualifier("igniteServer")
    private Ignite igniteServer;

    /**
     * 发送次数统计
     * @param topic
     */
    public void addSend(String topic){
        IgniteCache<String,MessageInfo> igniteCache = igniteServer.getOrCreateCache(MessageInfo.MESSAGE_CACHE);
        Lock lock = igniteCache.lock(topic);
        try {
            lock.lock();
            MessageInfo messageInfo = igniteCache.get(topic);
            messageInfo.addSendCount();
            igniteCache.put(topic,messageInfo);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 接收次数统计
     * @param topic
     */
    public void addRecevice(String topic){
        IgniteCache<String,MessageInfo> igniteCache = igniteServer.cache(MessageInfo.MESSAGE_CACHE);
        Lock lock = igniteCache.lock(topic);
        try {
            lock.lock();
            MessageInfo messageInfo = igniteCache.get(topic);
            messageInfo.addReceviceCount();
            igniteCache.put(topic,messageInfo);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 错误次数统计
     * @param topic
     */
    public void addError(String topic){
        IgniteCache<String,MessageInfo> igniteCache = igniteServer.cache(MessageInfo.MESSAGE_CACHE);
        Lock lock = igniteCache.lock(topic);
        try {
            lock.lock();
            MessageInfo messageInfo = igniteCache.get(topic);
            messageInfo.addErrorCount();
            igniteCache.put(topic,messageInfo);
        }finally {
            lock.unlock();
        }
    }
}
