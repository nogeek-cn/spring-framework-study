package com.darian.spring.framework.beans;


import com.darian.spring.framework.core.FacotryBean;

public class DarianBeanWrapper extends FacotryBean {

    // 还会用到 观察者模式。
    // 1. 支持事件响应，会有一个监听
    private DarianBeanPostProcessor postProcessor;

    public DarianBeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(DarianBeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    private Object wrapperInstance;
    // 原生的通过反射 new 出来，要把包装起来，存下来
    private Object orginalInstance;

    public DarianBeanWrapper(Object instance){
        this.wrapperInstance = instance;
        this.orginalInstance = instance;
    }

    public Object getWrappedInstance(){
        return this.wrapperInstance;
    }

    // 返回代理以后的 Class
    // 可能会是这个 $Proxy0
    public Class<?> getWrappedClass(){
        return this.wrapperInstance.getClass();
    }
}
