package com.darian.spring.framework.context;

import com.darian.spring.framework.beans.DarianBeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DarianDefaultListableBeanFactory extends DarianAbstractApplicationContext {

    // beanDefinitionMap 用来保存配置信息
    protected Map<String, DarianBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, DarianBeanDefinition>();

    @Override
    protected void onRefresh() {

    }

    @Override
    protected void refreshBeanFactory() {

    }
}
