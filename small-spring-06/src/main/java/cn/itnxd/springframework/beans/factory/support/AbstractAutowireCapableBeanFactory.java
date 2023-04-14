package cn.itnxd.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanReference;
import cn.itnxd.springframework.beans.factory.config.InstantiationStrategy;

import java.lang.reflect.Constructor;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:54
 * @Version 1.0
 * @Description AbstractBeanFactory 的实现类，同样是抽象类，只实现 createBean 方法
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{

    // 添加：持有实例化策略来根据策略实例化对象(默认为cglib策略)
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

    /**
     * 实现父抽奖类 AbstractBeanFactory 其中一个抽象方法 createBean
     *
     * 添加：创建 Bean 支持入参
     *
     * @param beanName
     * @param beanDefinition
     * @return
     * @throws BeansException
     */
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            // 1. 根据 BeanDefinition 创建 Bean
            bean = createBeanInstance(beanName, beanDefinition, args);
            // 2. 对 Bean 进行属性填充
            applyPropertyValues(beanName, beanDefinition, bean);
        } catch (BeansException e) {
            throw new BeansException("初始化Bean失败: ", e);
        }

        // 3. 添加到单例缓存 map
        addSingleton(beanName, bean);
        return bean;
    }

    /**
     * 抽取创建Bean的逻辑，调用本类持有的实例化策略进行实例化
     *
     * @param beanName
     * @param beanDefinition
     * @param args
     * @return
     */
    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException{
        // 1. 获取所有构造器
        Constructor ctor = null;
        Constructor[] declaredConstructors = beanDefinition.getBeanClass().getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            // 2. 简单比较参数数量即可（忽略类型比较）
            if(args != null && args.length == constructor.getParameterCount()) {
                ctor = constructor;
                break;
            }
        }
        // 3. 根据实例化策略创建对象
        return getInstantiationStrategy().instantiate(beanDefinition, beanName, ctor, args);
    }

    /**
     * 对实例化完成的 Bean 进行属性填充
     * @param beanName
     * @param beanDefinition
     * @param bean
     */
    protected void applyPropertyValues(String beanName, BeanDefinition beanDefinition, Object bean) {
        try {
            // 1. 获取 BeanDefinition 保存的 PV 集合
            PropertyValue[] propertyValues = beanDefinition.getPropertyValues().getPropertyValues();
            for (PropertyValue pv : propertyValues) {
                String name = pv.getName();
                Object value = pv.getValue();
                // 2. 属性是一个 Bean，递归创建所依赖的 Bean
                if (value instanceof BeanReference) {
                    BeanReference beanReference = (BeanReference) value;
                    value = getBean(beanReference.getBeanName());
                }
                // 3. 将属性 k v 设置到 bean 对象中
                BeanUtil.setFieldValue(bean, name, value);
            }
        } catch (Exception e) {
            throw new BeansException("为 Bean 【" + beanName + "】设置属性失败！");
        }
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
