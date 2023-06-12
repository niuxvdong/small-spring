package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

import java.util.*;

/**
 * @Author niuxudong
 * @Date 2023/4/9 20:02
 * @Version 1.0
 * @Description BeanFactory 的核心实现类，继承抽象类 AbstractAutowireCapableBeanFactory（具有了 BeanFactory 和 AbstractBeanFactory 等一连串的功能实现），
 *
 *              本类具有了获取 BeanDefinition 和注册 BeanDefinition 的能力
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry, ConfigurableListableBeanFactory {

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 实现抽象类 AbstractBeanFactory 定义的抽象方法获取 BeanDefinition
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new BeansException(beanName + "没有被定义过");
        }
        return beanDefinition;
    }

    /**
     * 实现 BeanDefinitionRegistry 的接口，注册 BeanDefinition
     * @param beanName
     * @param beanDefinition
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    /**
     * 实现顶层BeanFactory的根据类型获取bean的方法
     *
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> result = new HashMap<>();
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            Class beanClass = beanDefinition.getBeanClass();
            // beanClass 是 type 或者是 type 的子类为true
            if (type.isAssignableFrom(beanClass)) {
                T bean = (T) getBean(beanName);
                result.put(beanName, bean);
            }
        });
        return result;
    }

    /**
     * 实现ListableBeanFactory的方法
     * @return
     */
    @Override
    public String[] getBeanDefinitionNames() {
        Set<String> beanNames = beanDefinitionMap.keySet();
        return beanNames.toArray(new String[beanNames.size()]);
    }

    /**
     * 实现ConfigurableListableBeanFactory的创建单实例方法
     *
     * 增加：支持懒加载
     *
     * @throws BeansException
     */
    @Override
    public void preInstantiateSingletons() throws BeansException {
        // 对每个beanName都调用一次get方法即可，从一无所有到全都有
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            // 单例且非懒加载才会在容器启动进行创建
            if (beanDefinition.isSingleton() && !beanDefinition.isLazyInit()) {
                this.getBean(beanName);
            }
        });
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    public <T> T getBean(Class<T> requiredType) throws BeansException {
        List<String> beanNames = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?> beanClass = entry.getValue().getBeanClass();
            // beanClass 是需求类型或子类
            if (requiredType.isAssignableFrom(beanClass)) {
                beanNames.add(entry.getKey());
            }
        }
        if (beanNames.size() == 1) {
            return getBean(beanNames.get(0), requiredType);
        }
        throw new BeansException(requiredType + "期待单例Bean，但是发现了 " + beanNames.size() + ": " + beanNames);
    }
}
