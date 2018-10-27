package com.darian.springframeworktest.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "application-common.xml",
                "application-beans.xml",
                "application-jdbc.xml");
        Object member = context.getBean("member");

    }
}
