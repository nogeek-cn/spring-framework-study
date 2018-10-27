package com.darian.spring.framework.aop;



import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Pattern;

// 默认就用 JKD 动态代理
public class DarianAopProxy implements InvocationHandler {

    private DarianAopConfig config;

    private Object target;

    // 把原生的对象传进来
    public Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    public void setConfig(DarianAopConfig config){
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        // 作用: 利用 AOP 细项，自己去实现一个 TransactionManager
        // 需要补充，把 Method 的异常拿到，把 method 的方法拿到
        // 把 Mehod 的参数拿到
        // args 就是实参。
        // 客户端的操作 API， 服务的端的 Server 的 API


        // 通过原生方法去找，通过代理方法去 Map 中是找不到的。

        // 在原始方法调用以前要执行增强的代码
        if(config.contains(m)){
            DarianAopConfig.DarianAspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
        Object obj = null;


        try {
            // 反射调用原始的方法
            obj = method.invoke(this.target, args);
        } catch (Exception e) {
//            e.getClass();
//            if(e instanceof Exception){
//                conn.rollback();
//            }
        }

        // 在原始方式调用以后要执行增强的代码
        if(config.contains(m)){
            DarianAopConfig.DarianAspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }

        // 将最原始的返回值返回出去
        return obj;
    }
}
