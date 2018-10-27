package com.darian.spring.demo.mvc.action;


import com.darian.spring.demo.service.DemoService;
import com.darian.spring.framework.annotation.DarianAutowried;
import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianRequestMapping;
import com.darian.spring.framework.annotation.DarianRequestParam;
import com.darian.spring.framework.webmvc.DarianModelAndView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@DarianController
@DarianRequestMapping("/demo")
public class DemoAction {

    @DarianAutowried
    private DemoService demoService;

    @DarianRequestMapping("/query.json")
    public DarianModelAndView query(HttpServletRequest req, HttpServletResponse resp,
                                    @DarianRequestParam("name") String name) {
        String result = demoService.get(name);
        System.out.println(result);

        return out(resp, result);
    }

    @DarianRequestMapping("/edit.json")
    public DarianModelAndView edit(HttpServletRequest req, HttpServletResponse resp, Integer id) {
        return out(resp,null);
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
