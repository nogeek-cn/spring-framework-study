package com.darian.spring.demo.aspect;

public class LogAspect {
    // 在调用一个方法之前，执行 before 方法
    public void before(){
        // 这个方法中的逻辑，是我们自己去写的
        System.out.println("Invoker before method!!");
    }

    // 在调用一个方法之后，执行 after 方法
    public void after(){
        System.out.println("Invoker after method!!!");

    }
}
