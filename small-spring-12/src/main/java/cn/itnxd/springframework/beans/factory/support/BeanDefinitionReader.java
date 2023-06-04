package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.core.io.Resource;
import cn.itnxd.springframework.core.io.ResourceLoader;

/**
 * @Author niuxudong
 * @Date 2023/4/13 22:03
 * @Version 1.0
 * @Description Bean定义信息读取器，顶层接口
 *
 * 1. 获取BeanDefinitionRegistry注册中心（用来注册BeanDefinition）
 * 2. 获取资源加载器ResourceLoader（得到资源Resource）
 * 3. 装载 BeanDefinition 信息（1、2为3服务）
 */
public interface BeanDefinitionReader {

    /**
     * 获取BeanDefinitionRegistry注册中心（用来注册BeanDefinition）
     * @return
     */
    BeanDefinitionRegistry getRegistry();

    /**
     * 获取资源加载器ResourceLoader（得到资源Resource）
     * @return
     */
    ResourceLoader getResourceLoader();

    /**
     * 装载 BeanDefinition 信息（通过资源加载器得到资源，解析后得到BeanDefinition，通过注册中心进行注册）
     * @param resource
     * @throws BeansException
     */
    void loadBeanDefinitions(Resource resource) throws BeansException;

    void loadBeanDefinitions(String location) throws BeansException;

    void loadBeanDefinitions(String[] locations) throws BeansException;
}
