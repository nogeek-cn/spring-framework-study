package com.darian.spring.demo.mvc.action;

import com.darian.spring.demo.service.QueryService;
import com.darian.spring.framework.annotation.DarianAutowried;
import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianRequestMapping;
import com.darian.spring.framework.annotation.DarianRequestParam;
import com.darian.spring.framework.webmvc.DarianModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@DarianController
@DarianRequestMapping("/")
public class PageAction {

    @DarianAutowried
    private QueryService queryService;

    @DarianRequestMapping("/first.html")
    public DarianModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                    @DarianRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String ,Object> model = new HashMap<>();

        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");

        return new DarianModelAndView("first.html", model);
    }


}
