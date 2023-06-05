package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;

/**
 * @Author niuxudong
 * @Date 2023/4/17 22:42
 * @Version 1.0
 * @Description 用于在注册BeanDefinition信息之后实例化之前执行的修改BeanDefinition信息的操作（定义接口，没有具体实现类，由使用框架的人来实现）
 */
public interface BeanFactoryPostProcessor {

    /**
     * 在 BeanDefinition 注册完成后，Bean实例化之前，提供修改BeanDefinition属性的机制
     *
     * @param beanFactory
     * @throws BeansException
     */
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
