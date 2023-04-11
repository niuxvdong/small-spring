package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
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
            //bean = beanDefinition.getBeanClass().newInstance();
            bean = createBeanInstance(beanName, beanDefinition, args);
        } catch (BeansException e) {
            throw new BeansException("初始化Bean失败: ", e);
        }

        // 2. 添加到单例缓存 map
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
        Constructor[] declaredConstructors = beanDefinition.getBeanClass().getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            // 2. 简单比较参数数量即可（忽略类型比较）
            if(args != null && args.length == constructor.getParameterCount()) {
                // 3. 根据实例化策略创建对象
                return getInstantiationStrategy().instantiate(beanDefinition, beanName, constructor, args);
            }
        }
        // 4. 无匹配构造器抛出异常
        throw new BeansException("没有与当前参数匹配的构造器进行实例化，实例化失败。");
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
