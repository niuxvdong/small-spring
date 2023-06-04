package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.BeanFactoryAware;

/**
 * @Author niuxudong
 * @Date 2023/6/4 15:59
 * @Version 1.0
 * @Description 添加 BeanPostProcessor 来将自动代理融入 bean 生命周期
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor, BeanFactoryAware {

    /**
     * bean 实例化或初始化之前执行（与 postProcessBeforeInitialization 作用一样，传入参数不一样）
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;
}
