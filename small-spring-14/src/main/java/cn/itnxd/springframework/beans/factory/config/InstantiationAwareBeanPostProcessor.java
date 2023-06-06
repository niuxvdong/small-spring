package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.PropertyValues;
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

    /**
     * 增加对属性 @Value 或 @Autowired 注解的解析设置处理
     *
     * 执行时机：实例化之后，设置属性之前执行。对实例化好的 Bean 设置注解标注的属性
     *
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException;
}
