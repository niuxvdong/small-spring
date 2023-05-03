package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;

import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/17 23:06
 * @Version 1.0
 * @Description 是顶层接口BeanFactory的拓展，增加获取Bean的方法
 */
public interface ListableBeanFactory extends BeanFactory{

    /**
     * 拓展获取bean的方法，根据类型获取bean，返回一个指定类型的所有实例bean
     *
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

    /**
     * 返回容器中注册过的所有BeanDefinition信息
     *
     * @return
     */
    String[] getBeanDefinitionNames();
}
