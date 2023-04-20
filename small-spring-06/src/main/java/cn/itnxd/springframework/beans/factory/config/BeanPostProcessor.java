package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/4/17 22:47
 * @Version 1.0
 * @Description 在 Bean 实例化之后，提供修改Bean实例的机制（定义接口，没有具体实现类，由使用框架的人来实现）
 */
public interface BeanPostProcessor {

    /**
     * 见名知意，即在 Bean 实例化完成之后执行初始化方法之前（属性填充，空bean）进行修改Bean实例的机制
     *
     * @param bean
     * @param beanName
     * @return 返回处理完成的bean
     * @throws BeansException
     */
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

    /**
     * 见名知意，即在 Bean 实例化完成之后执行初始化方法之后（属性填充，非空Bean）进行修改Bean实例的机制
     *
     * @param bean
     * @param beanName
     * @return 返回处理完成的bean
     * @throws BeansException
     */
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
}
