package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.InstantiationStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @Author niuxudong
 * @Date 2023/4/11 0:08
 * @Version 1.0
 * @Description JDK实例化策略
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {

    /**
     *
     * @param beanDefinition BeanDefinition 信息
     * @param beanName BeanName
     * @param ctor 构造器（会包含构造器的参数类型和数量）
     * @param args 构造器参数（匹配ctor的参数value）
     * @return
     * @throws BeansException
     */
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            if (ctor != null) {
                return beanClass.getDeclaredConstructor(ctor.getParameterTypes()).newInstance(args);
            } else {
                return beanClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new BeansException("jdk实例化【" + beanClass.getName() + "】失败。", e);
        }
    }
}
