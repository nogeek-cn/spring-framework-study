package com.darian.spring.annotation;

import javax.swing.*;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DarianController {

    String value() default "";
}
