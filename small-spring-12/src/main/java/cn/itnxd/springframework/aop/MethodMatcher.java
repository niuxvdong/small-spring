package cn.itnxd.springframework.aop;

import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/5/28 17:32
 * @Version 1.0
 * @Description 顶层方法匹配器
 */
public interface MethodMatcher {

    /**
     * 判断方法和目标对象是否匹配
     *
     * @param method
     * @param targetClass
     * @return
     */
    boolean matches(Method method, Class<?> targetClass);
}
