package com.darian.spring.framework.beans;



/**
 * 用做事件监听的
 **/
public class DarianBeanPostProcessor {


    public Object postProcessBeforeInitialization(Object bean, String beanName)  {
        return bean;
    }


    public Object postProcessAfterInitialization(Object bean, String beanName)  {
        return bean;
    }
}
