package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

/**
 * @Author niuxudong
 * @Date 2023/4/17 23:17
 * @Version 1.0
 * @Description 多继承，从前到后：具有根据类型获取Bean的能力，处理初始化前后的beanPostProcessor，
 *              以及添加BeanPostProcessor的能力（包括父接口的过去单例bean，获取父类BeanFactory）
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

    /**
     * 根据beanName获取BeanDefinition的方法
     *
     * @param beanName
     * @return
     */
    BeanDefinition getBeanDefinitionName(String beanName);

    /**
     * 提前实例化所有单例实例的方法
     *
     * @throws BeansException
     */
    void preInstantiateSingletons() throws BeansException;
}
