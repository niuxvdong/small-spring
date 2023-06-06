package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;

/**
 * @Author niuxudong
 * @Date 2023/4/11 0:05
 * @Version 1.0
 * @Description 一个实例化策略接口
 */
public interface InstantiationStrategy {

    /**
     * 实例化接口
     *
     * @param beanDefinition BeanDefinition 信息
     * @param beanName BeanName
     * @param ctor 构造器
     * @param args 构造器参数
     * @return 返回实例化对象
     * @throws BeansException
     */
    Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException;
}
