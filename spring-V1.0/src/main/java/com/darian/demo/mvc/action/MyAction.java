package com.darian.demo.mvc.action;


import com.darian.demo.service.IDemoService;
import com.darian.spring.annotation.DarianAutowried;
import com.darian.spring.annotation.DarianController;
import com.darian.spring.annotation.DarianRequestMapping;

@DarianController
public class MyAction {

    @DarianAutowried
    IDemoService demoService;

    @DarianRequestMapping("/index.html")
    public void query() {

    }

}
