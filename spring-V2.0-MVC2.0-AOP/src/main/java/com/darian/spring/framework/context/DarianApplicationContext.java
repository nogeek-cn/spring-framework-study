package com.darian.spring.framework.context;


import com.darian.spring.framework.annotation.DarianAutowried;
import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianService;
import com.darian.spring.framework.aop.DarianAopConfig;
import com.darian.spring.framework.beans.DarianBeanDefinition;
import com.darian.spring.framework.beans.DarianBeanPostProcessor;
import com.darian.spring.framework.beans.DarianBeanWrapper;
import com.darian.spring.framework.context.support.DarianBeanDefinitionReader;
import com.darian.spring.framework.core.DarianBeanFactory;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DarianApplicationContext extends DarianDefaultListableBeanFactory implements DarianBeanFactory {

    private String[] configLocations;

    private DarianBeanDefinitionReader reader;


    // 用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new HashMap<>();

    // 存储所有被代理过的对象
    private Map<String, DarianBeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

    public DarianApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        this.refresh();
    }

    public void refresh() {
        // 定位
        this.reader = new DarianBeanDefinitionReader(configLocations);

        // 加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        // 注册
        doRegistry(beanDefinitions);

        // 依赖注入（lazy_init = false）要执行依赖注入
        // 在这里自动调用 getBean 方法。
        doAutowrited();

//        DemoAction demoAction = (DemoAction) this.getBean("demoAction");
//        demoAction.query(null, null, "darian");
//        System.out.println();

    }


    // 开始执行自动化的依赖注入
    private void doAutowrited() {
        for (Map.Entry<String, DarianBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                Object obj = getBean(beanName);
                System.out.println(obj.getClass());
            }
        }

        for (Map.Entry<String, DarianBeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()) {
            populateBean(beanWrapperEntry.getKey(), beanWrapperEntry.getValue().getOrginalInstance());

        }
        System.out.println("==========");

    }

    public void populateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();
        // 只负责，对应的 controller , service
        if (!(clazz.isAnnotationPresent(DarianController.class) ||
                clazz.isAnnotationPresent(DarianService.class))) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(DarianAutowried.class)) {
                continue;
            }
            DarianAutowried autowried = field.getAnnotation(DarianAutowried.class);
            String autowriedBeanName = autowried.value().trim();
            if ("".equals(autowriedBeanName)) {
                autowriedBeanName = field.getType().getName();
            }
            field.setAccessible(true);
            try {
                field.set(instance, this.beanWrapperMap.get(autowriedBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }
    }


    // 真正的将 beanDefinitions 注册到 beanDefinitionMap 中
    private void doRegistry(List<String> beanDefinitionList) {
        // beanName 有三种情况
        // 1. 默认是类名首字母小写
        // 2. 自定义名字
        // 3. 接口注入
        try {
            for (String className : beanDefinitionList) {

                Class<?> beanClass = Class.forName(className);
                //
                // 如果是一个接口，是不能实例化的。
                // 用它的实现类来实例化
                if (beanClass.isInterface()) {
                    continue;
                }
                DarianBeanDefinition darianBeanDefinition = reader.registerBean(className);
                if (darianBeanDefinition != null) {

//                    this.beanDefinitionMap.put(darianBeanDefinition.getBeanClassName(), darianBeanDefinition);
                    this.beanDefinitionMap.put(darianBeanDefinition.getFactoryBeanName(), darianBeanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    // 如果是多个实现类，只能覆盖
                    // 为什么？因为 Spring 没有那么智能，就是这么傻。
                    // 这个时候，可以自定义名字
                    this.beanDefinitionMap.put(i.getName(), darianBeanDefinition);
                }
                // 到这里位置，容器初始化完毕。
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 通过读取 BeanDefinition 中的信息
     * 然后，通过反射机制创造一个实例并返回
     * Spring 做法是，不会吧最原始对象放出去，会有一个BeanWrapper 来进行一次包装
     * 装饰器模式：
     * 1. 保留原来的 OOP 关系
     * 2. 我需要对它进行扩展，增强（为了以后的 AOP 打基础）
     **/
    @Override
    public Object getBean(String beanName) {
        DarianBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        String className = beanDefinition.getBeanClassName();

        try {
            // 生成通知事件，
            DarianBeanPostProcessor beanPostProcessor = new DarianBeanPostProcessor();

            Object instance = instantionBean(beanDefinition);
            if (null == instance) {
                return null;
            }

            // 在实例初始化以前调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

            DarianBeanWrapper darianBeanWrapper = new DarianBeanWrapper(instance);
            darianBeanWrapper.setAopConfig(instantionAopConfig(beanDefinition));
            this.beanWrapperMap.put(beanName, darianBeanWrapper);

            // 在实例初始化后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

//            populateBean(beanName, instance);

            // 通过这样一调用，相当于给我们自己流油留有了可操作的空间。
            return this.beanWrapperMap.get(beanName).getWrappedInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private DarianAopConfig instantionAopConfig(DarianBeanDefinition beanDefinition) throws Exception {
        DarianAopConfig config = new DarianAopConfig();
        String expression = reader.getConfig().getProperty("pointcut");
        String[] before = reader.getConfig().getProperty("aspectBefore").split("\\s");
        String[] after = reader.getConfig().getProperty("aspectAfter").split("\\s");

        String className = beanDefinition.getBeanClassName();
        Class<?> clazz = Class.forName(className);

        Pattern pattern = Pattern.compile(expression);
        Class<?> aspectClass = Class.forName(before[0]);

        // 这里得到的方法是原生的方法
        for (Method m : clazz.getMethods()) {

            // public java.lang.String com.darian.spring.demo.service.impl.ModifyServiceImpl.add(java.lang.String, java.lang.String)
            Matcher matcher = pattern.matcher(m.toString());
            if(matcher.matches()) {
                // 能满足切面规则的类，添加到 AOP 配置中
                config.put(m, aspectClass.newInstance(), new Method[]{aspectClass.getMethod(before[1]), aspectClass.getMethod(after[1])});
            }
        }
        return config;
    }

    // 传一个 DarianBeanDefinition，就返回一个实例 Bean
    private Object instantionBean(DarianBeanDefinition darianBeanDefinition) {
        String beanClassName = darianBeanDefinition.getBeanClassName();
        Object instance = null;
        try {

            // 默认是单例，不做判断了。没有保证单例
            //-----
            // 因为根据 Class 才能确定一个类是否有实例
            if (this.beanCacheMap.containsKey(beanClassName)) {
                instance = this.beanCacheMap.get(beanClassName);
            } else {
                Class<?> clazz = Class.forName(beanClassName);
                instance = clazz.newInstance();
                this.beanCacheMap.put(beanClassName, instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    public String[] getBeanDefinitionNames(){
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }


}
