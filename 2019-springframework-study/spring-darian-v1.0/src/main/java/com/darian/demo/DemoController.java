package com.darian.demo;

import com.darian.mvc.v1.annotation.DarianAutowrited;
import com.darian.mvc.v1.annotation.DarianController;
import com.darian.mvc.v1.annotation.DarianRequestMapping;
import com.darian.mvc.v1.annotation.DarianRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@DarianController
@DarianRequestMapping("/demo")
public class DemoController {

    @DarianAutowrited
    private DemoService demoService;

    @DarianRequestMapping("/query.json")
    public String query(HttpServletRequest req, HttpServletResponse resp,
                      @DarianRequestParam("name") String name) {
        String result = demoService.get(name);
        System.err.println(result);
        return result;
    }
}
