package com.darian.demo.service.impl;


import com.darian.demo.service.IDemoService;
import com.darian.spring.annotation.DarianService;

@DarianService
public class DemoService implements IDemoService {

    public String get(String name) {
        return "My name is " + name;
    }

}
