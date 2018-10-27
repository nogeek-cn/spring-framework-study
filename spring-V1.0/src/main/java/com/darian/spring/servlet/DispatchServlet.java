package com.darian.spring.servlet;

import com.darian.demo.mvc.action.DemoAction;
import com.darian.spring.annotation.DarianAutowried;
import com.darian.spring.annotation.DarianController;
import com.darian.spring.annotation.DarianService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private Map<String, Object> iocBeanMap = new ConcurrentHashMap<String, Object>();

    private List<String> classNames = new ArrayList<String>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("-------- 调用 doPost -------------");

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 开始初始化的进程

        // 定位
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 加载
        doScanner((String) contextConfig.get("scanPackage"));

        // 注册
        doRegistry();

        // 自动 依赖注入
        // 在 Spring 中式通过调用 getBean 方法才触发依赖注入的
        doAutowried();

        DemoAction demoAction = (DemoAction) iocBeanMap.get("demoAction");
        demoAction.query(null, null, "darian");

        // 如果是 Spring MVC 会多设计一个 HandlerMapping
        // 将 @Requestmapping 中配置的 URL 和一个 method 关联上
        // 以便于从浏览器获得用户输入的 URL 后，能够找到具体执行的 Method 通过反射去调用
        initHandlerMapping();
    }

    private void initHandlerMapping() {
    }

    private void doRegistry() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                // 在 Spring 中用的多个子方法来处理的。
                // parseArray, parseMap
                if (clazz.isAnnotationPresent(DarianController.class)) {
                    String beanName = StringlowerFirstCase(clazz.getSimpleName());
                    // 在 Spring 中，在这个阶段是不会直接 put instance，这里 put 的是 BeanDefinition
                    iocBeanMap.put(beanName, clazz.newInstance());

                } else if (clazz.isAnnotationPresent(DarianService.class)) {
                    DarianService service = clazz.getAnnotation(DarianService.class);

                    // 默认用类名首字母注入
                    // 如果自己定义了 beanName，那么有限使用自己定义的 BeanName
                    // 如果是一个接口，使用接口的类型去自动注入

                    // 在 Spring 中，同样会分别调用不同的放啊发， autowriedByName  autowriedByType
                    String beanName = service.value();
                    if ("".equals(beanName.trim())) {
                        beanName = StringlowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    iocBeanMap.put(beanName, instance);

                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        iocBeanMap.put(i.getName(), instance);
                    }


                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doAutowried() {
        if (iocBeanMap.isEmpty()) {
            return;
        }
        iocBeanMap.entrySet().forEach(entry -> {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(DarianAutowried.class)) {
                    continue;
                }
                DarianAutowried autowried = field.getAnnotation(DarianAutowried.class);
                String beanName = autowried.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), iocBeanMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));


        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    private void doLoadConfig(String location) {
        // 在 Spring 中是通过 Reader 去查找和定位的。
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 变成小写
     **/
    private static String StringlowerFirstCase(String string) {
        char[] chars = string.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
