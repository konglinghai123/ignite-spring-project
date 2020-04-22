package com.github.kong.ignite.spring.boot.admin.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessageInfo implements Serializable {

    public final static String MESSAGE_CACHE = "messageInfo";

    Map<String,NodeInfo> nodeInfos = new HashMap<>();

    private String topic;

    private String des;

    private boolean isBroadcast;

    private Date createTime;

    private Long sendCount = 0L;

    private Long receviceCount = 0L;

    private Long errorCount = 0L;

    public MessageInfo(){

    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getSendCount() {
        return sendCount;
    }

    public void setSendCount(Long sendCount) {
        this.sendCount = sendCount;
    }

    public Long getReceviceCount() {
        return receviceCount;
    }

    public void setReceviceCount(Long receviceCount) {
        this.receviceCount = receviceCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public void addNode(NodeInfo nodeInfo){
        nodeInfos.put(nodeInfo.getId(),nodeInfo);
    }

    public Map<String, NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public void setNodeInfos(Map<String, NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    public void addSendCount(){
        this.sendCount = sendCount + 1;
    }

    public void addReceviceCount(){
        this.receviceCount = receviceCount + 1;
    }

    public void addErrorCount(){
        this.errorCount = errorCount + 1;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        isBroadcast = broadcast;
    }
}
