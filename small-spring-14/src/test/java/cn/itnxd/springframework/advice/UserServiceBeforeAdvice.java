package cn.itnxd.springframework.advice;

import cn.itnxd.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/6/4 16:52
 * @Version 1.0
 * @Description 上一节我们实现的是 MethodInterceptor，这里我们不再进行实现，而是实现各类的 advice
 */
public class UserServiceBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("MethodBeforeAdvice 前置方法拦截：" + method.getName());
    }
}
