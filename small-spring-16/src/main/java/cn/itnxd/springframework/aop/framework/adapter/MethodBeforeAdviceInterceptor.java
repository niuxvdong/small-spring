package cn.itnxd.springframework.aop.framework.adapter;

import cn.itnxd.springframework.aop.MethodBeforeAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @Author niuxudong
 * @Date 2023/6/3 10:24
 * @Version 1.0
 * @Description 方法前置通知拦截器，实现 MethodBeforeAdvice 通知
 * MethodInterceptor 也是继承自 advice，且本类持有 advice
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptor {

    // 持有 advice 方法前置通知
    private MethodBeforeAdvice advice;

    public MethodBeforeAdviceInterceptor() {
    }

    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 拦截器真正执行方法之前执行通知的前置方法 advice.before
        this.advice.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }

    public MethodBeforeAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(MethodBeforeAdvice advice) {
        this.advice = advice;
    }
}
