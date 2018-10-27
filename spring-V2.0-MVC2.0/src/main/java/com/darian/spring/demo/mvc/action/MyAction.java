package com.darian.spring.demo.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.darian.spring.demo.service.ModifyService;
import com.darian.spring.demo.service.QueryService;
import com.darian.spring.framework.annotation.DarianAutowried;
import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianRequestMapping;
import com.darian.spring.framework.annotation.DarianRequestParam;
import com.darian.spring.framework.webmvc.DarianModelAndView;


/**
 * 公布接口url
 * @author Tom
 *
 */
@DarianController
@DarianRequestMapping("/web")
public class MyAction {

    @DarianAutowried
    QueryService queryService;
    @DarianAutowried
    ModifyService modifyService;

    @DarianRequestMapping("/query.json")
    public DarianModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @DarianRequestParam("name") String name){
        String result = queryService.query(name);
        System.out.println(result);
        return out(response,result);
    }

    @DarianRequestMapping("/add*.json")
    public DarianModelAndView add(HttpServletRequest request,HttpServletResponse response,
                              @DarianRequestParam("name") String name,@DarianRequestParam("addr") String addr){
        String result = modifyService.add(name,addr);
        return out(response,result);
    }

    @DarianRequestMapping("/remove.json")
    public DarianModelAndView remove(HttpServletRequest request,HttpServletResponse response,
                                 @DarianRequestParam("id") Integer id){
        String result = modifyService.remove(id);
        return out(response,result);
    }

    @DarianRequestMapping("/edit.json")
    public DarianModelAndView edit(HttpServletRequest request,HttpServletResponse response,
                               @DarianRequestParam("id") Integer id,
                               @DarianRequestParam("name") String name){
        String result = modifyService.edit(id,name);
        return out(response,result);
    }



    private DarianModelAndView out(HttpServletResponse resp, String str){
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
