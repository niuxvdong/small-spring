package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.DisposableBean;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // 增加：存放 disposableBean
    private Map<String, DisposableBean> disposableBeans = new HashMap<>();

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
     * 一个 public 方法，保存单例对象到 map 中
     * 可以被继承本类的子类调用，主要包括：AbstractBeanFactory 以及继承的 DefaultListableBeanFactory 调用
     * @param beanName
     * @param singletonObject
     */
    @Override
    public void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }

    /**
     * 注册 disposableBean 到单例bean注册中心保存
     * @param beanName
     * @param disposableBean
     */
    public void registerDisposableBean(String beanName, DisposableBean disposableBean) {
        disposableBeans.put(beanName, disposableBean);
    }

    /**
     * 通过接口 disposableBean 方式销毁单例 bean 的方法
     */
    public void destroySingletons() {
        List<String> beanNames = new ArrayList<>(disposableBeans.keySet());
        for (String beanName : beanNames) {
            DisposableBean disposableBean = disposableBeans.remove(beanName);
            try {
                disposableBean.destroy();
            } catch (BeansException e) {
                throw new BeansException("执行销毁 bean 方法时抛出异常 e: {}", e);
            }
        }
    }
}
