package com.darian.mvc.v1.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DarianRequestParam {
    String value() default "";
}
