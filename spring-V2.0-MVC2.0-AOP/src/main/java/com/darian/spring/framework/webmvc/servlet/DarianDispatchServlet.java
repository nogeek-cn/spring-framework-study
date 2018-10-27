package com.darian.spring.framework.webmvc.servlet;


import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianRequestMapping;
import com.darian.spring.framework.annotation.DarianRequestParam;
import com.darian.spring.framework.aop.DarianAopProxy;
import com.darian.spring.framework.aop.DarianAopProxyUtils;
import com.darian.spring.framework.context.DarianApplicationContext;
import com.darian.spring.framework.webmvc.DarianHandlerMapping;
import com.darian.spring.framework.webmvc.DarianModelAndView;
import com.darian.spring.framework.webmvc.DarianHandlerAdapter;
import com.darian.spring.framework.webmvc.DarianViewResolver;
import jdk.nashorn.internal.ir.CatchNode;

import javax.print.attribute.standard.MediaSize;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


// Servlet 只是作为一个 MVC 的启动入口
public class DarianDispatchServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

//    private Map<String, DarianHandlerMapping> handlerMapping = new HashMap<>();

    // 课后再去思考一下这样设计的经典之处
    // DarianHandlerMapping 最核心的设计，也是最经典的
    // 它牛逼到直接干掉了 Struts、Webwork 等 MVC 框架
    private List<DarianHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<DarianHandlerMapping, DarianHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<DarianViewResolver> viewResolvers = new ArrayList<>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        // 相当于把 IOC 容器初始化了
        DarianApplicationContext context = new DarianApplicationContext(config.getInitParameter(LOCATION));

        initStrategies(context);
    }

    private void initStrategies(DarianApplicationContext context) {
        // 有九个策略
        // 针对于每个用户请求，都会经过一些处理的策略之后，最终才能会有结果输出。
        // 每种策略可以自定义敢于，但是最终的结果都是一致
        // ModelAndView

        // ==========这里就是传输中的 Spring MVC 中的九大组件=========

        initMultipartResolver(context); // 文件上传解析，如果请求类型是 multipart 将通过 MultipartREsolver
        initLocaleResolver(context); // 本地化解析
        initThemeResolver(context);// 主题解析

        /**
         * HandlerMapping 用来保存 Controller 中配置的 RequestMapping 和 Method 的一个对应关系
         */
        initHandlerMappings(context);// 通过 HandlerMapping，将请求映射到处理器
        /**
         * HandlerAdapters 用来动态匹配 Method 参数，包括类转换，动态赋值
         */
        initHandlerAdapters(context);// 通过 HandlerAdapter 继续进行多类型的参数的动态匹配

        initHandlerExceptionResolvers(context);// 如果执行过程中遇到异常，将交给 HandlerExceptionResolvers
        initRequestToViewNameTranslator(context); // 直接解析请求到视图名
        /**
         * 通过 ViewResolvers 实现动态模板的解析
         * 自己解析一套模板语言  自己写一套
         */
        initViewResolvers(context);// 通过 viewResolver 解析逻辑试图到具体试图实现
        initFlashMapManager(context); // flash 映射管理器


    }

    // 将 Controller 中配置的 RequestMapping 和 Method 的进行一一对应关系
    private void initHandlerMappings(DarianApplicationContext context) {
        // 按照我们通常的理解应该是一个 Map
        // Map<String, Method> map;
        // map.put(url, Method);

        // 首先从容器中取到所有的容器
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                // 到了 MVC 层，对外提供的方法只有一个 getBean 方法
                // 返回的对象不是 BeanWrapper，怎么办？
                Object proxy = context.getBean(beanName);

                Object controller = DarianAopProxyUtils.getTargetObject(proxy);
                Class<?> clazz = controller.getClass();
                // 但是不是所有的牛奶都叫特仑苏
                if (!clazz.isAnnotationPresent(DarianController.class)) {
                    continue;
                }
                String baseUrl = "";
                if (clazz.isAnnotationPresent(DarianRequestMapping.class)) {
                    DarianRequestMapping requestMapping = clazz.getAnnotation(DarianRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                // 扫描所有的 public 方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(DarianRequestMapping.class)) {
                        continue;
                    }
                    DarianRequestMapping requestMapping = method.getAnnotation(DarianRequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new DarianHandlerMapping(pattern, controller, method));
                    System.out.println("Mapping:" + regex + "," + method);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // 把方法的参数进行动态的配置
    private void initHandlerAdapters(DarianApplicationContext context) {
        // 在初始化阶段，我们能做的就是，将这些参数的名字或者类型按照一定的顺序保存下来，
        // 因为后边用反射调用的时候，传的形参是一个数组
        // 可以通过记录这些参数的位置的 insdex，挨个从数组中填值，这样的话，就和参数的顺序无关了

        for (DarianHandlerMapping handlerMapping : this.handlerMappings) {
            // 每一个方法有一个参数列表，那么这里保存的是形参列表
            Map<String, Integer> paramMappping = new HashMap<>();

            // 这里只是处理了命名参数
            Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof DarianRequestParam) {
                        String paramName = ((DarianRequestParam) a).value();
                        if (!"".equals(paramName.trim())) {
                            paramMappping.put(paramName, i);
                        }
                    }
                }
            }

            // 接下里，我们处理非命名参数
            // 只处理 request 和 response
            Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class ||
                        type == HttpServletResponse.class) {
                    paramMappping.put(type.getName(), i);
                }
            }

            this.handlerAdapters.put(handlerMapping, new DarianHandlerAdapter(paramMappping));
        }
    }

    private void initViewResolvers(DarianApplicationContext context) {
        // 在页面敲一个 http://localhost/first.html
        // 解决一个页面名字和模板文件关联的问题。
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new DarianViewResolver(template.getName(), template));
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("-------- 调用 doPost -------------");
//        String url = req.getRequestURI();
//        String contextPath = req.getContextPath();
//
//        url = url.replace(contextPath, "").replaceAll("/+", "/");
//        DarianHandlerMapping handler = handlerMapping.get(url);
//
//        // 对象， 方法名才能调用
//        // 对象要从 IOC 容器中去获取。
//        try {
//            DarianModelAndView mv = (DarianModelAndView) handler.getMethod().invoke(handler.getController(), null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/> \r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", "\r\n") + "\n <font color='green'><i>Copyright@DarianMVC</i></font>");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        // 根据用户请求的 URL 来获得一个 Handler
        DarianHandlerMapping handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("<font size='25' color='red'>404 Not Found</font><br/><font color='green'><i>Copyright@DarianMVC</i></font>\r\n");
            return;
        }

        DarianHandlerAdapter ha = getHandlerAdapter(handler);

        // 这一步只是调用方法，得到返回值
        DarianModelAndView mv = ha.handler(req, resp, handler);

        // 这一步才是真正的输出
        processDispatchResult(resp, mv);

    }

    private void processDispatchResult(HttpServletResponse resp, DarianModelAndView mv) throws Exception {
        // 调用 DarianViewResolver 的 resolveView 方法
        if (null == mv) {
            return;
        }
        if (this.viewResolvers.isEmpty()) {
            return;
        }

        for (DarianViewResolver viewResolver : this.viewResolvers) {
            if (!mv.getViewName().equals(viewResolver.getViewName())) {
                continue;
            }
            String out = viewResolver.viewResolver(mv);
            if (out != null) {
                resp.getWriter().write(out);
                break;
            }
        }


    }

    private DarianHandlerAdapter getHandlerAdapter(DarianHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        return this.handlerAdapters.get(handler);
    }

    private DarianHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (DarianHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;
    }


    private void initFlashMapManager(DarianApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(DarianApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(DarianApplicationContext context) {
    }

    private void initThemeResolver(DarianApplicationContext context) {
    }

    private void initLocaleResolver(DarianApplicationContext context) {
    }

    private void initMultipartResolver(DarianApplicationContext context) {
    }
}
