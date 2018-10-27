package com.darian.spring.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DarianRequestParam {
    String value() default "";
}
