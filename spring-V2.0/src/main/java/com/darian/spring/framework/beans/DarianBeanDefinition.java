package com.darian.spring.framework.beans;

import com.sun.xml.internal.ws.api.ha.StickyFeature;

import javax.lang.model.element.NestingKind;

/**
 * 用来存储配置文件中的信息
 * 相当于保存在内存中的配置
 * <br>Darian
 **/
public class DarianBeanDefinition {

    private String beanClassName;
    private boolean lazyInit = false;
    private String factoryBeanName ;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    //    public void setBeanClassName(String beanClassName) {
//
//    }
//
//    public String getBeanClassName() {
//        return null;
//    }
//
//    public void setFacotryBeanName(String factoryBeanName){
//
//    }
//
//    public String getFactoryBeanName(){
//        return null;
//    }
//
//    public void setLazyInit(boolean lazyInit){
//
//    }
//
//    public boolean isLazyInit(){
//        return false;
//    }

    // 默认就是单例
}
