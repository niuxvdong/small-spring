package cn.itnxd.springframework.aop.framework;

import cn.itnxd.springframework.aop.AdvisedSupport;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author niuxudong
 * @Date 2023/5/28 20:38
 * @Version 1.0
 * @Description jdk 动态代理实现
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    // 持有通知支持类 AdvisedSupport 方便使用
    private final AdvisedSupport advised;

    public JdkDynamicAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    /**
     * 实现获取代理方法
     * @return
     */
    @Override
    public Object getProxy() {
        // 类加载器，目标类的class，InvocationHandler即本类自己this
        return Proxy.newProxyInstance(getClass().getClassLoader(), advised.getTargetSource().getTargetClass(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 判断切点是否支持该方法
        if (advised.getMethodMatcher().matches(method, advised.getTargetSource().getTarget().getClass())) {
            // 拿到方法拦截器
            MethodInterceptor methodInterceptor = advised.getMethodInterceptor();
            // 调用拦截器进行方法拦截（传入方法执行器）
            // 方法拦截器的 invoke 逻辑由用户实现（可以在 method.invoke 增加逻辑）
            return methodInterceptor.invoke(new ReflectiveMethodInvocation(advised.getTargetSource().getTarget(), method, args));
        }
        // 切点不支持则直接执行 invoke
        return method.invoke(advised.getTargetSource().getTarget(), args);
    }
}
