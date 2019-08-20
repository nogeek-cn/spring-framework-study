
package com.darian.mvc.v1.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DarianAutowrited {
    String value() default "";


}