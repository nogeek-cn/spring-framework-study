
package com.darian.mvc.v1.annotation;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DarianService {
    String value() default "";

    ;
}