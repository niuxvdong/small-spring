package cn.itnxd.springframework.context.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanFactoryPostProcessor;
import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;
import cn.itnxd.springframework.context.ApplicationEvent;
import cn.itnxd.springframework.context.ApplicationListener;
import cn.itnxd.springframework.context.ConfigurableApplicationContext;
import cn.itnxd.springframework.context.event.ContextClosedEvent;
import cn.itnxd.springframework.context.event.ContextRefreshedEvent;
import cn.itnxd.springframework.context.event.SimpleApplicationEventMulticaster;
import cn.itnxd.springframework.core.convert.ConversionService;
import cn.itnxd.springframework.core.io.DefaultResourceLoader;

import java.util.Collection;
import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/18 21:33
 * @Version 1.0
 * @Description 顶层接口ApplicationContext的抽象实现类，主要实现父接口定义的额refresh流程方法。
 *
 * 这里会发现AbstractApplicationContext实现大多数接口时候都是调用的BeanFactory的子类 beans.factory.support包下的实现。
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

    // ApplicationEventMulticaster 的 beanName
    public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

    private SimpleApplicationEventMulticaster applicationEventMulticaster;

    /**
     * 实现父接口的refresh刷新容器方法
     *
     *
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

        // 3. 增加：refresh 流程增加 processor：ApplicationContextAwareProcessor，拥有感知能力
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

        // 4. bean实例化之前执行BeanFactoryPostProcessor
        invokeBeanFactoryPostProcessors(beanFactory);

        // 5. bean初始化之前，注册所有的BeanPostProcessor到容器保存
        registerBeanPostProcessors(beanFactory);

        // 6. 初始化事件发布者
        initApplicationEventMulticaster();

        // 7. 注册事件监听器
        registerListeners();

        // 8. 开始实例化，先实例化单例Bean
        //beanFactory.preInstantiateSingletons();
        // 修改：（二合一）注册类型转换器 和 提前实例化单例bean
        finishBeanFactoryInitialization(beanFactory);

        // 9. 发布事件：容器refresh完成事件
        finishRefresh();
    }

    /**
     * 在实例化 bean 之前先进行类型转换器注册
     * @param beanFactory
     */
    private void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        // 设置类型转换器
        if (beanFactory.containsBean("conversionService")) {
            Object conversionService = beanFactory.getBean("conversionService");
            if (conversionService instanceof ConversionService) {
                // 通过 BeanFactory 保存到 AbstractBeanFactory 持有的 conversionService 中
                beanFactory.setConversionService((ConversionService) conversionService);
            }
        }
        // 提前实例化单例bean
        beanFactory.preInstantiateSingletons();
    }

    /**
     * 实例化之前初始化 事件广播器/事件发布者
     */
    private void initApplicationEventMulticaster() {
        // 1. 获取 BeanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        // 2. 创建 ApplicationEventMulticaster
        applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        // 3. 添加到单例池
        beanFactory.addSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
    }

    /**
     * 初始化完发布者注册监听器
     */
    private void registerListeners() {
        // 1. 获取所有监听器
        Collection<ApplicationListener> applicationListeners = getBeansOfType(ApplicationListener.class).values();
        for (ApplicationListener applicationListener : applicationListeners) {
            // 2. 添加到 Set 中进行保存
            applicationEventMulticaster.addApplicationListener(applicationListener);
        }
    }

    /**
     * refresh 完成后，发布容器刷新完成事件
     */
    private void finishRefresh() {
        publishEvent(new ContextRefreshedEvent(this));
    }

    /**
     *
     * @param event
     */
    @Override
    public void publishEvent(ApplicationEvent event) {
        applicationEventMulticaster.multicastEvent(event);
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
     * 补充顶层BeanFactory的getBean方法实现
     *
     * @param beanName
     * @param args
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName, Object... args) throws BeansException {
        return getBeanFactory().getBean(beanName, args);
    }

    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return getBeanFactory().getBean(requiredType);
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

    /**
     * 判断容器是否有 bean
     * @param name
     * @return
     */
    @Override
    public boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    /**
     * 虚拟机关闭时执行的操作
     */
    public void close() {
        doClose();
    }

    protected void doClose() {

        // 增加发布容器关闭事件
        this.publishEvent(new ContextClosedEvent(this));

        destroyBeans();
    }

    /**
     * 真正执行销毁方法的地方
     */
    protected void destroyBeans() {
        getBeanFactory().destroySingletons();
    }

    /**
     * 注册 shutdownHook 在虚拟机关闭时候调用 disposableBean.destroy() 方法
     */
    public void registerShutdownHook() {
        // 创建一个线程注入doClose方法在虚拟机关闭时候调用
        Thread shutdownHook = new Thread(this::doClose);
        // 虚拟机关闭时候调用
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
}
