package cn.itnxd.springframework.processor;

import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanFactoryPostProcessor;
import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @Author niuxudong
 * @Date 2023/4/20 23:43
 * @Version 1.0
 * @Description
 */
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /**
     * 实现父接口方法，实例化之前进行操作
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("userService");
        // 在实例化之前增加没有定义的属性
        beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("company", "新增-华为"));
    }
}
