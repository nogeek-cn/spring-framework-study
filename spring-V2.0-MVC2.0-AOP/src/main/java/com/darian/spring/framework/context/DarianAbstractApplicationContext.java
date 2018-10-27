package com.darian.spring.framework.context;

public abstract class DarianAbstractApplicationContext {

    // 提供给子类重写
    protected void onRefresh(){
    }

    protected abstract void refreshBeanFactory();
}
