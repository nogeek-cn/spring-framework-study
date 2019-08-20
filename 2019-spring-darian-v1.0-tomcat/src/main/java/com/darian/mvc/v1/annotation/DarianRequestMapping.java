
package com.darian.mvc.v1.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,
        ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DarianRequestMapping {
    String value() default "";
}