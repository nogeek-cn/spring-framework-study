package com.darian.spring.demo.mvc.action;


import com.darian.spring.demo.service.IDemoService;
import com.darian.spring.framework.annotation.DarianAutowried;
import com.darian.spring.framework.annotation.DarianController;
import com.darian.spring.framework.annotation.DarianRequestMapping;

@DarianController
public class MyAction {

    @DarianAutowried
    IDemoService demoService;

    @DarianRequestMapping("/index.html")
    public void query() {

    }

}
