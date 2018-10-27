package com.darian.spring.framework.webmvc.servlet;


import com.darian.spring.framework.context.DarianApplicationContext;
import com.darian.spring.framework.webmvc.DarianHandlerMapping;
import com.darian.spring.framework.webmvc.DarianModelAndView;
import com.darian.spring.framework.webmvc.DarianHandlerAdapter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;



// Servlet 只是作为一个 MVC 的启动入口
public class DispatchServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

//    private Map<String, DarianHandlerMapping> handlerMapping = new HashMap<>();

    // 课后再去思考一下这样设计的经典之处
    // DarianHandlerMapping 最核心的设计，也是最经典的
    // 它牛逼到直接干掉了 Struts、Webwork 等 MVC 框架
    private List<DarianHandlerMapping> handlerMappings = new ArrayList<>();

    private List<DarianHandlerAdapter> handlerAdapters = new ArrayList<>();


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

    }

    private void initHandlerAdapters(DarianApplicationContext context) {

    }

    private void initViewResolvers(DarianApplicationContext context) {

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

//        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        DarianHandlerMapping handler = getHandler(req);

        DarianHandlerAdapter ha = getHandlerAdapter(handler);

        DarianModelAndView mv = ha.handler(req, resp, handler);

        processDispatchResult(resp, mv);
    }

    private void processDispatchResult(HttpServletResponse resp, DarianModelAndView mv) {
        // 调用 DarianViewResolver 的 resolveView 方法
    }

    private DarianHandlerAdapter getHandlerAdapter(DarianHandlerMapping handler) {
        return null;
    }

    private DarianHandlerMapping getHandler(HttpServletRequest req) {
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
