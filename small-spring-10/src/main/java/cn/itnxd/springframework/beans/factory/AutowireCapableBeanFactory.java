package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/4/17 23:29
 * @Version 1.0
 * @Description 是顶层接口BeanFactory的拓展，用来处理BeanPostProcessors的前后置方法
 */
public interface AutowireCapableBeanFactory extends BeanFactory{

    /**
     * 用来执行BeanPostProcessors的beanPostProcessorsBeforeInitialization方法
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) throws BeansException;

    /**
     * 用来执行BeanPostProcessors的beanPostProcessorsAfterInitialization方法
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) throws BeansException;


}
