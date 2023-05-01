package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:31
 * @Version 1.0
 * @Description 本类实现顶层单例接口 SingletonBeanRegistry
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    // 存放单例对象
    private Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * 实现顶层单例接口的唯一个获取单例对象的方法
     * @param beanName
     * @return
     */
    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    /**
     * 一个 protected 方法，保存单例对象到 map 中
     * 可以被继承本类的子类调用，主要包括：AbstractBeanFactory 以及继承的 DefaultListableBeanFactory 调用
     * @param beanName
     * @param singletonObject
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }
}
