package cn.itnxd.springframework.context.annotation;

import java.lang.annotation.*;

/**
 * @Author niuxudong
 * @Date 2023/6/4 22:23
 * @Version 1.0
 * @Description
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

    String value() default "singleton";
}
