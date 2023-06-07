package cn.itnxd.springframework.aop.framework;

import cn.itnxd.springframework.aop.AdvisedSupport;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/5/28 21:07
 * @Version 1.0
 * @Description Cglib 动态代理实现
 */
public class CglibAopProxy implements AopProxy{

    // 持有通知支持类 AdvisedSupport 方便使用
    private final AdvisedSupport advised;

    public CglibAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        Enhancer enhancer = new Enhancer();
        // 设置目标对象类
        enhancer.setSuperclass(advised.getTargetSource().getTarget().getClass());
        // 设置接口
        enhancer.setInterfaces(advised.getTargetSource().getTargetClass());
        // 设置回调接口（即方法拦截器）
        enhancer.setCallback(new DynamicAdvisedInterceptor(advised));
        return enhancer.create();
    }

    /**
     * 注意此处的MethodInterceptor是cglib中的接口，advised中的MethodInterceptor的AOP联盟中定义的接口，因此定义此类做适配
     */
    private static class DynamicAdvisedInterceptor implements MethodInterceptor {

        // 持有通知支持类 AdvisedSupport 方便使用
        private final AdvisedSupport advised;

        private DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        /**
         * 拦截器的拦截方法
         * @param o
         * @param method
         * @param objects
         * @param methodProxy
         * @return
         * @throws Throwable
         */
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            // 创建 cglib 方法执行器
            CglibMethodInvocation methodInvocation = new CglibMethodInvocation(advised.getTargetSource().getTarget(), method, objects, methodProxy);
            // 切点表达式与方法匹配
            if (advised.getMethodMatcher().matches(method, advised.getTargetSource().getTarget().getClass())) {
                // 匹配成功则调用 方法拦截器 执行方法，传入方法执行器 CglibMethodInvocation
                return advised.getMethodInterceptor().invoke(methodInvocation);
            }
            // 不匹配则继续执行下一个拦截器
            return methodInvocation.proceed();
        }
    }

    /**
     * cglib 方法执行器，继承自 ReflectiveMethodInvocation
     */
    private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

        // 持有一个 cglib 的方法代理
        private final MethodProxy methodProxy;

        public CglibMethodInvocation(Object target, Method method, Object[] arguments, MethodProxy methodProxy) {
            super(target, method, arguments);
            this.methodProxy = methodProxy;
        }

        /**
         * 重写了 ReflectiveMethodInvocation 的 proceed 方法
         * @return
         * @throws Throwable
         */
        @Override
        public Object proceed() throws Throwable {
            // 通过 cglib 提供的方法代理去执行目标方法
            return this.methodProxy.invoke(this.target, this.arguments);
        }
    }
}
