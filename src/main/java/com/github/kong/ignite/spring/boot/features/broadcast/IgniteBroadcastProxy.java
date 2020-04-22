package com.github.kong.ignite.spring.boot.features.broadcast;

import com.github.kong.ignite.spring.boot.context.IgniteApplicationContextHolder;
import org.apache.ignite.lang.IgniteCallable;
import org.dozer.util.ReflectionUtils;

import java.lang.reflect.Method;


/**
 * 利用共有的spring上下文执行bean方法
 */
public class IgniteBroadcastProxy implements IgniteCallable {

    private String methodName;

    private Object[] args;

    private Class targetClass;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Object call() throws Exception {
        Object springBeanObject = IgniteApplicationContextHolder.getBean(targetClass);
        Method method = ReflectionUtils.getMethod(springBeanObject,methodName);
        return  method.invoke(springBeanObject,args);
    }



}
