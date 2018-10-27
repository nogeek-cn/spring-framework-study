package com.darian.spring.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/***
 * 只是对 Application 的 Expression 的封装
 * 目标代理对象的一个方法要增强
 * 由用户自己实现的业务逻辑去增强
 * 配置文件的目的，告诉 Spring ，哪些类的那些方法需要增强，增强的内容是什么？
 * 对配置文件中所体现的内容进行封装
 */
public class DarianAopConfig {

    // 以目标对象需要增强的 Method 作为 key，需要增强的代码内容作为 value
    private Map<Method, DarianAspect> points = new HashMap<>();

    public void put(Method target, Object aspect, Method[] points) {
        this.points.put(target, new DarianAspect(aspect, points));
    }

    public DarianAspect get(Method method) {
        return this.points.get(method);
    }

    public boolean contains(Method method) {
        return this.points.containsKey(method);
    }

    // 对增强的代码的封装
    public class DarianAspect {
        private Object aspect;// 待会将 LogAspect 这个对象赋值给它
        private Method[] points; // 会将 LogAspect 的 before 方法 和 after 方法赋值进来

        public DarianAspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }
    }
}
