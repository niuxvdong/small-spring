package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

/**
 * @Author niuxudong
 * @Date 2023/4/9 20:06
 * @Version 1.0
 * @Description BeanDefinitionRegistry 接口，定义 BeanDefinition 注册接口
 */
public interface BeanDefinitionRegistry {

    /**
     * 只定义 BeanDefinition 注册接口
     * @param beanName
     * @param beanDefinition
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * beanName 是否已存在
     * @param beanName
     * @return
     */
    boolean containsBeanDefinition(String beanName);
}
