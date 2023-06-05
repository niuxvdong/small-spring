package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.core.io.DefaultResourceLoader;
import cn.itnxd.springframework.core.io.ResourceLoader;

/**
 * @Author niuxudong
 * @Date 2023/4/13 22:13
 * @Version 1.0
 * @Description BeanDefinitionReader 的实现类
 *
 * 只需要实现获取两个工具（注册中心和资源加载器）的方法即可
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader{

    private BeanDefinitionRegistry registry;

    private ResourceLoader resourceLoader;

    /**
     * 单参构造
     * @param registry
     */
    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this(registry, new DefaultResourceLoader());
    }

    /**
     * 两参构造
     * @param registry
     * @param resourceLoader
     */
    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        this.registry = registry;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 实现父接口BeanDefinitionReader的获取注册中心方法
     * @return
     */
    @Override
    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }

    /**
     * 实现父接口BeanDefinitionReader的获取资源加载器方法
     * @return
     */
    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
