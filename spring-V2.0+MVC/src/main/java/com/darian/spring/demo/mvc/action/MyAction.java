package com.darian.spring.demo.mvc.action;


import com.darian.spring.demo.service.DemoService;
import com.darian.spring.framework.annotation.DarianAutowried;
import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianRequestMapping;

@DarianController
public class MyAction {

    @DarianAutowried
    DemoService demoService;

    @DarianRequestMapping("/index.html")
    public void query() {

    }

}
