package com.github.kong.ignite.spring.boot.features.message;

import com.github.kong.ignite.spring.boot.features.message.model.MessageModel;
import org.apache.ignite.lang.IgniteBiPredicate;

import java.util.UUID;

/**
 * 屏蔽 ignite 相关的接口实现
 * @param <T>
 */
public interface IgniteMessageRecevicer<T> extends IgniteBiPredicate<UUID,MessageModel<T>> {

}
