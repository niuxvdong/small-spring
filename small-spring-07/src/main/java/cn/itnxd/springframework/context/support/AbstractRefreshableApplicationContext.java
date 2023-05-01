package cn.itnxd.springframework.context.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;
import cn.itnxd.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @Author niuxudong
 * @Date 2023/4/18 22:10
 * @Version 1.0
 * @Description 继承AbstractApplicationContext，实现抽象父类没有实现的两个方法 refreshBeanFactory、getBeanFactory
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext{

    private DefaultListableBeanFactory beanFactory;

    /**
     * 实现抽象父类的刷新BeanFactory方法
     *
     * @throws BeansException
     */
    @Override
    protected void refreshBeanFactory() throws BeansException {
        // 1. 创建BeanFactory
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        this.beanFactory = beanFactory;
        // 2. 加载BeanDefinition信息到容器
        loadBeanDefinitions(beanFactory);
    }

    /**
     * 创建一个我们之前实现的最底层的工厂，即DefaultListableBeanFactory。
     *
     * 这里ApplicationContext拥有了BeanFactory的所有功能。
     *
     * @return
     */
    private DefaultListableBeanFactory createBeanFactory() {
        return new DefaultListableBeanFactory();
    }

    /**
     * 加载所有BeanDefinition信息到容器，本方法由子类实现。
     *
     * @param beanFactory
     */
    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory);

    /**
     * 获取创建的BeanFactory
     *
     * @return
     */
    @Override
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
