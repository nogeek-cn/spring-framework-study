package com.darian.demo.mvc.action;


import com.darian.demo.service.IDemoService;
import com.darian.spring.annotation.DarianAutowried;
import com.darian.spring.annotation.DarianController;
import com.darian.spring.annotation.DarianRequestMapping;
import com.darian.spring.annotation.DarianRequestParam;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@DarianController
@DarianRequestMapping("/demo")
public class DemoAction {

    @DarianAutowried
    private IDemoService demoService;

    @DarianRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @DarianRequestParam("name") String name) {
        String result = demoService.get(name);
        System.out.println(result);
//		try {
//			resp.getWriter().write(result);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

    @DarianRequestMapping("/edit.json")
    public void edit(HttpServletRequest req, HttpServletResponse resp, Integer id) {

    }

}
