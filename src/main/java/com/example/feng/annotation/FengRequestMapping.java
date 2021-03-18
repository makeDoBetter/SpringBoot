package com.example.feng.annotation;


import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fengjirong
 * @date 2021/3/11 14:20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FengRequestMapping {

    String value() default "";

    RequestMethod[] method() default {};
}
