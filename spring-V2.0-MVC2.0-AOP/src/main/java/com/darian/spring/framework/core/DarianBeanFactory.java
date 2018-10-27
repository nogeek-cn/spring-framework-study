package com.darian.spring.framework.core;

public interface DarianBeanFactory {

    /**
     * 根据 BeanName 从 IOC 容器之中获得一个实例 Bean，
     **/
    Object getBean(String beanName);
}
