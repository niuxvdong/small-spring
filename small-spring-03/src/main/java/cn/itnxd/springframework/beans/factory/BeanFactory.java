package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:27
 * @Version 1.0
 * @Description 顶层 Bean 工厂接口
 */
public interface BeanFactory {

    /**
     * 唯一的一个方法，获取 Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName) throws BeansException;

    /**
     * 增加有入参的获取 Bean 的接口
     * @param beanName
     * @param args
     * @return
     * @throws BeansException
     */
    Object getBean(String beanName, Object ...args) throws BeansException;
}
