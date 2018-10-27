package com.darian.spring.demo.service.impl;


import com.darian.spring.demo.service.IDemoService;
import com.darian.spring.framework.annotation.DarianService;

@DarianService
public class DemoService implements IDemoService {

    public String get(String name) {
        return "My name is " + name;
    }

}
