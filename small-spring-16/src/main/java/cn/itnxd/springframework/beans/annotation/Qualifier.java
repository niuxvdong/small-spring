package cn.itnxd.springframework.beans.annotation;

import java.lang.annotation.*;

/**
 * @Author niuxudong
 * @Date 2023/6/5 21:49
 * @Version 1.0
 * @Description 结合 @Autowired 使用，增加指定 beanName
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface Qualifier {

	String value() default "";
}