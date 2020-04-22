package com.github.kong.ignite.spring.boot.features.message;

import com.github.kong.ignite.spring.boot.features.message.model.MessageModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.cluster.ClusterGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * 消息发送者
 */
@Service
public class IgniteMessageSender {

    @Autowired
    @Qualifier("igniteClient")
    private Ignite igniteClent;

    @Autowired
    private IgniteMessageStatistical statistical;

    /**
     * 指定角色集群发送
     * @param role
     * @param topic
     * @param messageModel
     */
    public void toRemote(String role,String topic,MessageModel<?> messageModel){
        ClusterGroup clusterGroup = igniteClent.cluster().forRemotes().forServers();

        if(StringUtils.isNotBlank(role)){
            clusterGroup = clusterGroup.forAttribute("role",role);
        }

        IgniteMessaging msg = igniteClent.message(clusterGroup);

        msg.send(topic,messageModel);

        statistical.addSend(topic);
    }


    /**
     * 集群内发送
     * @param topic
     * @param messageModel
     */
    public void toRemote(String topic,MessageModel<?> messageModel){
        toRemote("",topic,messageModel);
    }





}
