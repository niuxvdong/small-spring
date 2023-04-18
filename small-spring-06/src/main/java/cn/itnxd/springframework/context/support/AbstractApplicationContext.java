package cn.itnxd.springframework.context.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanFactoryPostProcessor;
import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;
import cn.itnxd.springframework.context.ConfigurableApplicationContext;
import cn.itnxd.springframework.core.io.DefaultResourceLoader;

import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/18 21:33
 * @Version 1.0
 * @Description 顶层接口ApplicationContext的抽象实现类，主要实现父接口定义的额refresh流程方法。
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

    /**
     * 实现父接口的refresh刷新容器方法
     *
     * 本类只实现第三步和第四步！
     *
     * refreshBeanFactory、getBeanFactory 由本抽象类的子类进行实现。
     *
     * @throws BeansException
     */
    @Override
    public void refresh() throws BeansException {
        // 1. 刷新BeanFactory：创建BeanFactory，加载BeanDefinition到工厂
        refreshBeanFactory();

        // 2. 获取BeanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();

        // 3. bean实例化之前执行BeanFactoryPostProcessor
        invokeBeanFactoryPostProcessors(beanFactory);

        // 4. bean初始化之前，注册所有的BeanPostProcessor到容器保存
        registerBeanPostProcessors(beanFactory);

        // 5. 开始实例化，先实例化单例Bean
        beanFactory.preInstantiateSingletons();
    }

    /**
     * 刷新BeanFactory：创建BeanFactory，加载BeanDefinition到工厂
     *
     * @throws BeansException
     */
    protected abstract void refreshBeanFactory() throws BeansException;

    /**
     * 获取BeanFactory
     *
     * @return
     */
    protected abstract ConfigurableListableBeanFactory getBeanFactory();

    /**
     * bean实例化之前执行BeanFactoryPostProcessor
     *
     * @param beanFactory
     */
    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 1. 根据类型获取到容器中的所有BeanFactoryPostProcessor
        Map<String, BeanFactoryPostProcessor> beanFactoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);
        for (BeanFactoryPostProcessor beanFactoryPostProcessor : beanFactoryPostProcessorMap.values()) {
            // 2. 调用BeanFactoryPostProcessor的方法去执行processor
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    /**
     * bean初始化之前，注册所有的BeanPostProcessor到容器保存
     *
     * @param beanFactory
     */
    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 1. 获取到所有的BeanPostProcessor
        Map<String, BeanPostProcessor> beanPostProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorMap.values()) {
            // 2. 向BeanFactory中注册BeanPostProcessor保存
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
    }

    /**
     * 实现顶层BeanFactory的根据类型获取bean实例的方法
     *
     * @param beanName
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> T getBean(String beanName, Class<T> type) throws BeansException {
        return getBeanFactory().getBean(beanName, type);
    }

    /**
     * 实现父接口ListableBeanFactory的根据类型获取bean的方法
     *
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 重新实现顶层BeanFactory的根据beanName获取bean的方法，可以看到其实还是调用的AbstractBeanFactory的实现
     *
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName) throws BeansException {
        return getBeanFactory().getBean(beanName);
    }

    /**
     * 实现父接口ListableBeanFactory获取所有BeanDefinitionName的方法
     *
     * @return
     */
    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }
}
