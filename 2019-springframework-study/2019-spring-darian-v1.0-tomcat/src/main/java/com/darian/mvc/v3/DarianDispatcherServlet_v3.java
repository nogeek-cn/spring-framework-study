
package com.darian.mvc.v3;

import com.darian.mvc.v1.annotation.*;

import javax.naming.ldap.Control;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DarianDispatcherServlet_v3 extends HttpServlet {

    Logger LOGGER = Logger.getLogger(DarianDispatcherServlet_v3.class.getSimpleName());

    private static String SCANN_PACKAGE_KEY = "scannPackage";
    private Properties contenxtConfig = new Properties();

    private List<String> classNameList = new ArrayList<>();
    private Map<String, Object> ioc_map = new HashMap<>();
    private List<HandlerMapping> handler_mappings = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2. 扫描相关的类
        doScaner(contenxtConfig.getProperty(SCANN_PACKAGE_KEY));
        // 3. 初始化扫描到的类，并且将他们放入到 IOC 容器中
        doInstance();
        // 4. 完成依赖注入
        doAutowrited();
        // 5. 初始化 HandlerMapping
        initHandlerMapping();

        LOGGER.info("Darian small Spring Framework is init");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String url = req.getRequestURI().replaceAll("/+", "/");
        if (url.endsWith("/favicon.ico")) {
            LOGGER.info("\"/favicon.ico\" 不做处理");
            return;
        }
        // 6. 调用，运行阶段
        try {

            doDispatcher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();

            resp.getWriter().write("{\"code\":500,\"msg\":\"自定义DispatcherServlet#doDispatcher发生错误！！！\"}\n\n\n" +
                    "ExceptionMessage:[" + e.getMessage() + "]");
        }
    }

    private void doLoadConfig(String servletConfigPath) {
        try (InputStream fis = this.getClass().getClassLoader()
                .getResourceAsStream(servletConfigPath)) {
            contenxtConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void doScaner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(
                scanPackage.replaceAll("\\.", "/"));

        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                // 递归调用自己
                doScaner(scanPackage + "." + file.getName());
            } else {
                if (file.getName().endsWith(".class")) {
                    String className = scanPackage + "."
                            + file.getName().replaceAll(".class", "");
                    classNameList.add(className);
                }
            }
        }

    }

    private void doInstance() {
        // 初始化，为 DI 做准备
        if (classNameList.isEmpty()) {
            return;
        }
        try {
            for (String className : classNameList) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(DarianController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerfistCase(clazz.getSimpleName());
                    ioc_map.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(DarianService.class)) {
                    DarianService darianService = clazz.getAnnotation(DarianService.class);
                    String beanName = darianService.value();
                    // 1. 默认类名小写
                    // 2. 自定义 beanName

                    if ("".equals(beanName)) {
                        beanName = toLowerfistCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    ioc_map.put(beanName, instance);
                    // 3. 根据类型自动赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc_map.containsKey(i.getName())) {
                            throw new RuntimeException("The beanName [" + i.getName() + "] is exists !");
                        }
                        ioc_map.put(i.getName(), instance);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerfistCase(String className) {
        char[] chars = className.toCharArray();
        // 大写和小写 ASCII 码相差 32，只看 类名首字母大写
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doAutowrited() {
        if (ioc_map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc_map.entrySet()) {
            // Declared 所有的字段，private / protected / public
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(DarianAutowrited.class)) {
                    DarianAutowrited darianAutowrited = field.getAnnotation(DarianAutowrited.class);
                    String beanName = darianAutowrited.value()
                            .trim();
                    if ("".equals(beanName)) {
                        beanName = field.getType().getName();
                    }
                    // 强制赋值，
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(), ioc_map.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 初始话 url 和 method 的一对一 对应关系
    private void initHandlerMapping() {
        if (ioc_map.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc_map.entrySet()) {
            Object controller = entry.getValue();
            Class<?> clazz = controller.getClass();
            if (!clazz.isAnnotationPresent(DarianController.class)) {
                continue;
            }

            // 保存在类上边的 @DarianRequestMapping("/demo")
            String baseUrl = "";
            if (clazz.isAnnotationPresent(DarianRequestMapping.class)) {
                baseUrl = clazz.getAnnotation(DarianRequestMapping.class).value();
            }

            // 默认获取所有的 public 方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(DarianRequestMapping.class)) {
                    continue;
                }

                String url = baseUrl + "/" + method.getAnnotation(DarianRequestMapping.class).value();
                url = url.replaceAll("/+", "/");
                handler_mappings.add(new HandlerMapping(Pattern.compile(url), controller, method));
                LOGGER.info("mapping   url:[" + url + "], method:[" + method + "]");
            }
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");

        HandlerMapping handlerMapping = getHandlerMapping(req);
        if (handlerMapping == null) {
            throw new Exception("{\"code\":404,\"msg\":\"找不到对应的 mappings！: mapping_url:[" + handlerMapping.url + "]\"}");
        }

        Annotation[][] parameterAnnotations = handlerMapping.method.getParameterAnnotations();

        Class<?>[] paramterTypes = handlerMapping.paramterTypes;

        Object[] paramValues = new Object[paramterTypes.length];

        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", ",");

            if (!handlerMapping.paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            int index = handlerMapping.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramterTypes[index], value);
        }

        Integer reqIndex = handlerMapping.paramIndexMapping.get(HttpServletRequest.class.getName());
        if (reqIndex != null)
            paramValues[reqIndex] = req;
        Integer respIndex = handlerMapping.paramIndexMapping.get(HttpServletResponse.class.getName());
        if (respIndex != null)
            paramValues[respIndex] = resp;

        Object returnValue = handlerMapping.method.invoke(handlerMapping.controller, paramValues);
        if (returnValue == null || returnValue instanceof Void) {
            return;
        }
        resp.getWriter().write(returnValue.toString());

    }

    /***
     * 参数转换器
     */
    private Object convert(Class<?> paramterType, String value) {
        if (paramterType == Integer.class) {
            try {
                return Integer.valueOf(value);
            } catch (Exception e) {
                throw new RuntimeException("转化异常：[" + value + "]:type:[" + paramterType + "]");
            }
        }
        if (paramterType == String.class) {
            return String.valueOf(value);
        }
        if (paramterType == Double.class) {
            return Double.valueOf(value);
        }
        // ...
        return null;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        this.doPost(req, resp);
    }


    public HandlerMapping getHandlerMapping(HttpServletRequest req) {
        if (handler_mappings.isEmpty()) return null;
        String url = req.getRequestURI()
                .replaceAll(req.getContextPath(), "")
                .replaceAll("/+", "/");

        for (HandlerMapping mapping : this.handler_mappings) {
            Matcher matcher = mapping.url.matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return mapping;
        }
        return null;
    }


    // 保存 URL 和 method 的关系
    public class HandlerMapping {
        public Pattern url;
        public Method method;
        public Object controller;
        public Class<?>[] paramterTypes;

        // 形参列表
        // 参数的名字作为 key,参数的位置作为 值
        public Map<String, Integer> paramIndexMapping;

        public HandlerMapping(Pattern url, Object controller, Method method) {
            this.url = url;
            this.method = method;
            this.controller = controller;

            paramterTypes = method.getParameterTypes();

            paramIndexMapping = new HashMap<>();
            pubParamterIndexMapping(method);
        }

        private void pubParamterIndexMapping(Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == HttpServletRequest.class) {
                    paramIndexMapping.put(HttpServletRequest.class.getName(), i);
                }
                if (parameterTypes[i] == HttpServletResponse.class) {
                    paramIndexMapping.put(HttpServletResponse.class.getName(), i);
                }
            }

            // 提取方法中有的参数，
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {


                    if (annotation instanceof DarianRequestParam) {
                        String paramName = DarianRequestParam.class.cast(annotation).value();
                        if (!"".equals(paramName.trim())) {
                            paramIndexMapping.put(paramName, i);
                        }
                    }

                }
            }
        }
    }
}