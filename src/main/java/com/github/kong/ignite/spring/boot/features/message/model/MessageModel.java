package com.github.kong.ignite.spring.boot.features.message.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * ignite 消息封装
 * @param <T>
 */
public class MessageModel<T> implements Serializable{

    private final static long serialVersionUID = 1596333371406420499L;

    private String uuid = UUID.randomUUID().toString();
    private T Object;
    private Date createTime = new Date();

    public MessageModel() {

    }

    public MessageModel(T object) {
        Object = object;
    }

    public MessageModel(String uuid, T object, Date createTime) {
        this.uuid = uuid;
        Object = object;
        this.createTime = createTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public T getObject() {
        return Object;
    }

    public void setObject(T object) {
        Object = object;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "uuid='" + uuid + '\'' +
                ", Object=" + Object +
                ", createTime=" + createTime +
                '}';
    }
}
