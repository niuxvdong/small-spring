package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.ObjectFactory;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.DisposableBean;
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

    // 存放单例对象（一级缓存）
    private final Map<String, Object> singletonObjects = new HashMap<>();

    // 增加二级缓存 earlySingletonObjects
    private final Map<String, Object> earlySingletonObjects = new HashMap<>();

    // 增加三级缓存 singletonFactories
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();

    // 增加：存放 disposableBean
    private final Map<String, DisposableBean> disposableBeans = new HashMap<>();

    /**
     * 实现顶层单例接口的唯一个获取单例对象的方法
     *
     * 修改，先获取一级缓存 singletonObjects（设置属性之后的 Bean）
     * 空则再获取二级缓存获取 earlySingletonObjects（未设置属性的 Bean）
     * @param beanName
     * @return
     */
    @Override
    public Object getSingleton(String beanName) {
        // 先获取一级缓存（完整对象）
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null) {
            // 为空获取二级缓存（未设置属性的对象）
            singletonObject = earlySingletonObjects.get(beanName);
            if (singletonObject == null) {
                // 仍空则获取三级缓存（代理对象的引用）
                ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
                if (singletonFactory != null) {
                    // 三级缓存不空则获取代理对象引用返回
                    singletonObject = singletonFactory.getObject();
                    // 将三级缓存代理对象引用放进二级缓存
                    earlySingletonObjects.put(beanName, singletonObject);
                    // 删除三级缓存代理对象引用
                    singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    /**
     * 添加三级缓存
     * @param beanName
     * @param singletonFactory
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        singletonFactories.put(beanName, singletonFactory);
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
        // 增加：向一级缓存添加对象，需要将二级三级缓存清空
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
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
