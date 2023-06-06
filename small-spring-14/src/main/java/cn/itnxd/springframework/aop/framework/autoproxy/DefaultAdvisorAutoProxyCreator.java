package cn.itnxd.springframework.aop.framework.autoproxy;

import cn.itnxd.springframework.aop.*;
import cn.itnxd.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import cn.itnxd.springframework.aop.framework.ProxyFactory;
import cn.itnxd.springframework.beans.PropertyValues;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import cn.itnxd.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import java.util.Collection;

/**
 * @Author niuxudong
 * @Date 2023/6/4 16:02
 * @Version 1.0
 * @Description 特殊的 BeanPostProcessor，处理 aop 的切面逻辑
 */
public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor {

    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    /**
     * 废弃：这个在实例化前处理的 代理生成，防止返回 代理对象后影响 createBean 流程的后续操作，造成短路
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * 增加：通过 BeanPostProcessor 的后置处理中，修改 bean 实例，替换为代理对象（若有）
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 基础 bean (advice/pointcut/advisor) 不进行处理
        if (isInfrastructureClass(bean.getClass())) {
            return null;
        }

        // 获取容器所有的 aspectj 表达式通知
        Collection<AspectJExpressionPointcutAdvisor> advisors = beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class).values();
        for (AspectJExpressionPointcutAdvisor advisor : advisors) {
            ClassFilter classFilter = advisor.getPointcut().getClassFilter();
            // 类过滤器匹配则进行处理
            if (classFilter.matches(bean.getClass())) {
                AdvisedSupport advisedSupport = new AdvisedSupport();

                // 注意：改变自动代理融入时机，则这里的 bean 可能是 cglib 生成的实例，在动态代理获取 interface 时会有问题，
                // 因此在 targetSource 类中做一下判断处理
                TargetSource targetSource = new TargetSource(bean);

                // 为 advice 支持类填充需要的信息方便使用
                advisedSupport.setTargetSource(targetSource);
                advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
                // advisor 获取的 advice 就是各类通知，例如我们实现的 MethodBeforeAdvice（用户实现这些接口后）
                advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
                // 默认使用 jdk 动态代理
                advisedSupport.setProxyTargetClass(false);

                // 使用代理工厂进行创建代理对象
                return new ProxyFactory(advisedSupport).getProxy();
            }
        }
        return bean;
    }

    /**
     * 判断 bean 是否是基础 bean (advice/pointcut/advisor)
     * @param beanClass
     * @return
     */
    private boolean isInfrastructureClass(Class<?> beanClass) {
        return Advice.class.isAssignableFrom(beanClass)
                || Pointcut.class.isAssignableFrom(beanClass)
                || Advisor.class.isAssignableFrom(beanClass);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 职责分离，本类只处理 aop 切面代理对象的生成。
     * 本方法直接返回不做处理。
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        return pvs;
    }
}
