package cn.itnxd.springframework.advice;

import cn.itnxd.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/6/10 17:40
 * @Version 1.0
 * @Description
 */
public class ABeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("A 被代理：method 是" + method.getName());
    }
}
