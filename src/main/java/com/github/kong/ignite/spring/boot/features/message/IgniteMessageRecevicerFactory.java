package com.github.kong.ignite.spring.boot.features.message;

import com.github.kong.ignite.spring.boot.admin.model.MessageInfo;
import com.github.kong.ignite.spring.boot.admin.model.NodeInfo;
import com.github.kong.ignite.spring.boot.features.message.annotation.IgniteMessageListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.apache.ignite.events.EventType.EVT_NODE_FAILED;
import static org.apache.ignite.events.EventType.EVT_NODE_LEFT;

/**
 * 消息服务
 */
public class IgniteMessageRecevicerFactory
        implements ApplicationContextAware, InitializingBean, DisposableBean,Serializable {

    private final static long serialVersionUID = 2596393671406420499L;

    private Logger logger = LoggerFactory.getLogger(IgniteMessageRecevicerFactory.class);

    private Ignite igniteServer = null;

    private Ignite igniteClient = null;

    private String role = "";


    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {

        logger.info(">>>>>>>>>>> 正在初始化网格消息接收者...");

        registerDiscoveryEvtListener();

        Map<String, Object> messageBeanMap = applicationContext.getBeansWithAnnotation(IgniteMessageListener.class);
        if (messageBeanMap != null && messageBeanMap.size() > 0) {
            for (String beanName : messageBeanMap.keySet()) {
                Object serviceBean = messageBeanMap.get(beanName);
                // 校验是否实现了 IgniteBiPredicate 接口
                if (!(serviceBean instanceof IgniteMessageRecevicer)) {
                    throw new IgniteException("消息服务必须实现IgniteMessageRecevicer接口");
                }

                // spring4 和 spring5 的aop代理不一样，不能直接通过serviceBean.getClass().getAnnotation(IgniteMessageListener.class) 获取

                IgniteMessageListener igniteMessageListener = applicationContext.findAnnotationOnBean(beanName,IgniteMessageListener.class);

                if(igniteMessageListener != null){
                    String topic = igniteMessageListener.topic();

                    if(StringUtils.isBlank(topic)){
                        throw new IgniteException("消息主题不能为空");
                    }

                    deployMessage(igniteMessageListener, (IgniteBiPredicate<UUID, ?>) serviceBean);
                }

            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info(">>>>>>>>>>> 初始化消息接收者成功！");
    }

    @Override
    public void destroy() throws Exception {
        igniteServer.close();
    }

    public void setIgniteServer(Ignite igniteServer) {
        this.igniteServer = igniteServer;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setIgniteClient(Ignite igniteClient) {
        this.igniteClient = igniteClient;
    }

    public void deployMessage(IgniteMessageListener igniteMessageListener, IgniteBiPredicate<UUID, ?> aClass) {

        ClusterGroup clusterGroup = igniteServer.cluster().forLocal().forServers().forPredicate(new IgnitePredicate<ClusterNode>() {
            @Override
            public boolean apply(ClusterNode clusterNode) {
                return  StringUtils.equals(role, clusterNode.attribute("role"))&& !clusterNode.isClient();
            }
        });

        IgniteMessaging rmtMsg = igniteServer.message(clusterGroup);

        rmtMsg.localListen(igniteMessageListener.topic(),aClass);

        registerMessage(igniteMessageListener);

        logger.info("网格消息已监听【"+ igniteMessageListener.topic()+"】（"+ igniteMessageListener.des()+")");

    }

    /**
     * 监听节点离开事件，实时更新消息在线节点
     */
    public void registerDiscoveryEvtListener() {

        igniteClient.events().localListen(new IgnitePredicate<DiscoveryEvent>() {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean apply(DiscoveryEvent e) {

                String original = e.eventNode().id().toString();

                IgniteCache<String,MessageInfo> igniteCache = igniteClient.cache(MessageInfo.MESSAGE_CACHE);

                igniteCache.forEach(objectObjectEntry -> {
                    MessageInfo messageInfo =  objectObjectEntry.getValue();
                    messageInfo.getNodeInfos().remove(original);
                    igniteCache.put(messageInfo.getTopic(),messageInfo);
                });


                return true;
            }
        }, EVT_NODE_FAILED, EVT_NODE_LEFT);

    }

    /**
     * 将消息假如到缓存中
     */
    public void registerMessage(IgniteMessageListener messageListener){
        IgniteCache<String,MessageInfo> igniteCache = igniteServer.cache(MessageInfo.MESSAGE_CACHE);

        String topic = messageListener.topic();
        String des = messageListener.des();
        Boolean isBroadcast = messageListener.isBroadcast();

        ClusterNode node = igniteServer.cluster().forLocal().forServers().node();

        //节点信息
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(node.id().toString());
        nodeInfo.setName(node.attribute("name"));
        nodeInfo.setRole(node.attribute("role"));
        nodeInfo.setStartTime(node.attribute("startTime"));
        nodeInfo.setDes(node.attribute("des"));
        nodeInfo.setHostName(node.hostNames());

        MessageInfo messageInfo = igniteCache.get(topic);

        if(messageInfo == null){
            messageInfo = new MessageInfo();
            messageInfo.setTopic(topic);
            messageInfo.setDes(des);
            messageInfo.setBroadcast(isBroadcast);
            messageInfo.setCreateTime(new Date());
        }
        messageInfo.addNode(nodeInfo);
        igniteCache.put(topic,messageInfo);
    }

}
