package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:38
 * @Version 1.0
 * @Description 抽象Bean工厂，继承单例实现（具有单例注册能力），实现顶层Bean工厂接口
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {

    /**
     * 1. 实现顶层 BeanFactory 接口的唯一方法 <br>
     * 2. 这也是本抽象类 AbstractBeanFactory 的模板方法模式的体现，本方法即为模板方法，定义了整个骨架 <br>
     *
     * 3. 模板方法设计模式笔记：<a href="https://blog.itnxd.cn/article/behavioral-pattern-template-design-pattern">https://blog.itnxd.cn/article/behavioral-pattern-template-design-pattern</a>
     *
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName) throws BeansException {
        // 1. 获取单例 Bean
        Object bean = getSingleton(beanName);
        if (bean != null) {
            return bean;
        }
        // 2. 单例 Bean 不存在则创建 Bean
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        // 3. 根据 beanDefinition 创建 Bean
        return createBean(beanName, beanDefinition);
    }

    /**
     * 本抽象类只单纯实现getBean
     * 创建 Bean 由子类实现
     * @param beanName
     * @param beanDefinition
     * @return
     */
    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException;

    /**
     * 根据 beanName 获取 BeanDefinition 信息
     * @param beanName
     * @return
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;
}
