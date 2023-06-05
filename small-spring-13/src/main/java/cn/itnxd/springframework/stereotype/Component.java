package cn.itnxd.springframework.stereotype;

import java.lang.annotation.*;

/**
 * @Author niuxudong
 * @Date 2023/6/4 22:21
 * @Version 1.0
 * @Description
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    String value() default "";
}
