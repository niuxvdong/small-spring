package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/9 20:02
 * @Version 1.0
 * @Description BeanFactory 的核心实现类，继承抽象类 AbstractAutowireCapableBeanFactory（具有了 BeanFactory 和 AbstractBeanFactory 等一连串的功能实现），
 *
 *              本类具有了获取 BeanDefinition 和注册 BeanDefinition 的能力
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry{

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
}
