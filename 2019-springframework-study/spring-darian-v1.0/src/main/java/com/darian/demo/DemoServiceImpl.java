package com.darian.demo;

import com.darian.mvc.v1.annotation.DarianService;


@DarianService
public class DemoServiceImpl implements DemoService {

    @Override
    public String get(String name) {
        return "DemoServiceImpl#get[Hello, " + name + "]";
    }
}
