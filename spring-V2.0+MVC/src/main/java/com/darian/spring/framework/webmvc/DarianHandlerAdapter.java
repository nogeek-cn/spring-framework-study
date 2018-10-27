package com.darian.spring.framework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 专人干专事，解耦，
public class DarianHandlerAdapter {

    /***
     *
     * @param req
     * @param resp
     * @param handler 为什么要把 handler传进来
     *                因为 handler 中包含了 controller、method、url 传值
     * @return
     */
    public DarianModelAndView handler(HttpServletRequest req, HttpServletResponse resp, DarianHandlerMapping handler) {
        // 根据用户的请求的参数信息，跟 method 中的参数信息进行动态匹配
        // 这个 response 传进来的目的只有一个，只有一个， 只是为了将其赋值给方法参数，仅此而已
        // Spring 中规定了，任何一个方法都是一个自由的方法，只是适配，这个 Request, Response new 不出来，

        // 只有当用户传过来的 ModelAndView 为空的时候，才会 new 一个 默认的。
        return null;
    }
}
