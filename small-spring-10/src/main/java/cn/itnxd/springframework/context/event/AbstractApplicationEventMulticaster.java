package cn.itnxd.springframework.context.event;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.BeanFactoryAware;
import cn.itnxd.springframework.context.ApplicationEvent;
import cn.itnxd.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:42
 * @Version 1.0
 * @Description 事件广播器的抽象实现类：实现add和remove方法
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

    private BeanFactory beanFactory;

    // 存储所有的监听器
    public final Set<ApplicationListener<ApplicationEvent>> applicationListeners = new HashSet<>();

    /**
     * 实现父接口的添加监听器方法
     * @param listener
     */
    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.add((ApplicationListener<ApplicationEvent>) listener);
    }

    /**
     * 实现父接口的移除监听器方法
     * @param listener
     */
    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.remove(listener);
    }

    /**
     * 通过aware接口拿到所属的 BeanFactory
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
