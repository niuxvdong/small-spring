package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.InstantiationStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Constructor;

/**
 * @Author niuxudong
 * @Date 2023/4/11 21:39
 * @Version 1.0
 * @Description Cglib 实例化策略
 */
public class CglibSubclassingInstantiationStrategy implements InstantiationStrategy {

    /**
     * 引入Cglib依赖实现（根据父类实现子类）
     *
     * CGLIB是一个基于ASM的字节码处理框架，可以通过动态生成子类来实现AOP等功能
     *
     * 使用CGLIB创建代理类时，被代理类必须有默认构造函数。如果被代理类没有默认构造函数，
     * 可以使用Enhancer的setCallbackType方法设置回调函数接口，然后在回调函数实现类中调用有参构造函数来创建对象。
     *
     * @param beanDefinition BeanDefinition 信息
     * @param beanName BeanName
     * @param ctor 构造器
     * @param args 构造器参数
     * @return
     * @throws BeansException
     */
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        // 1. 创建Enhancer对象，用于生成代理类
        Enhancer enhancer = new Enhancer();
        // 2. 设置代理类的父类或接口
        enhancer.setSuperclass(beanDefinition.getBeanClass());
        // 3. 设置回调函数，即拦截方法的逻辑处理
        /*
          CGLIB的NoOp接口是一个空的方法拦截器，它可以在创建代理类时作为回调函数，用于对目标类的方法进行空处理，即不做任何操作，直接返回原方法的返回值。
          使用NoOp接口作为回调函数，可以在某些情况下提高代理类的性能，避免不必要的方法拦截和处理。

          注意：NoOp.INSTANCE是NoOp接口的一个实例，表示空的方法拦截器。
          使用NoOp接口作为回调函数时，目标类的方法必须是非final的，否则代理类无法覆盖该方法。
         */
        enhancer.setCallback(NoOp.INSTANCE);
        // 4. 生成代理类实例
        if (ctor == null) return enhancer.create();
        return enhancer.create(ctor.getParameterTypes(), args);
    }
}
