package cn.itnxd.springframework.aop.framework;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/5/28 20:55
 * @Version 1.0
 * @Description 方法拦截器 MethodInterceptor 的 MethodInvocation 方法调用器（参数保包装作用，方便使用）
 */
public class ReflectiveMethodInvocation implements MethodInvocation {

    // 目标对象
    protected final Object target;

    // 目标方法
    protected final Method method;

    // 构造方法参数
    protected final Object[] arguments;

    public ReflectiveMethodInvocation(Object target, Method method, Object[] arguments) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * 执行方法
     * @return
     * @throws Throwable
     */
    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target, arguments);
    }

    /**
     * 返回目标对象
     * @return
     */
    @Override
    public Object getThis() {
        return target;
    }

    /**
     * 返回目标方法
     * @return
     */
    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }
}
