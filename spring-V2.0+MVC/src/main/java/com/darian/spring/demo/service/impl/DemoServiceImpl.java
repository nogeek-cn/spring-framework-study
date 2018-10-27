package com.darian.spring.demo.service.impl;


import com.darian.spring.demo.service.DemoService;
import com.darian.spring.framework.annotation.DarianService;

@DarianService
public class DemoServiceImpl implements DemoService {

    public String get(String name) {
        return "My name is " + name;
    }

}
