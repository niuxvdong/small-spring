package cn.itnxd.springframework.context.event;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.context.ApplicationEvent;
import cn.itnxd.springframework.context.ApplicationListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:53
 * @Version 1.0
 * @Description 抽象事件广播器的子类，实现没有实现的方法：核心的广播方法
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster{

    public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
        setBeanFactory(beanFactory);
    }

    /**
     * 事件广播核心方法
     * @param event
     */
    @Override
    public void multicastEvent(ApplicationEvent event) {
        // 遍历所有的监听器
        for (ApplicationListener<ApplicationEvent> applicationListener : applicationListeners) {
            // 监听器支持对本事件处理再进行处理
            if (supportsEvent(applicationListener, event)) {
                applicationListener.onApplicationEvent(event);
            }
        }
    }

    /**
     * 判断监听器是否可以处理指定的事件
     *
     * @param applicationListener
     * @param event
     * @return
     */
    private boolean supportsEvent(ApplicationListener<ApplicationEvent> applicationListener, ApplicationEvent event) {
        // cglib实例化策略getClass获取到是代理类，jdk实例化策略获取到的是本类
        Class<? extends ApplicationListener> listenerClass = applicationListener.getClass();
        Class<?> actualClass = isCglibClass(listenerClass) ? listenerClass.getSuperclass() : listenerClass;

        // 获取 applicationListener 所实现的所有泛型参数的第一个，即这里的事件接口 ApplicationEvent
        Type type = actualClass.getGenericInterfaces()[0];
        // 获取到 ApplicationEvent 传入的具体子类 refresh 或是 close 事件
        Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        String className = actualTypeArgument.getTypeName();
        Class<?> eventClassName = null;
        try {
            eventClassName = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BeansException("事件类型不存在 【{" + className + "}】");
        }
        // 判断event是不是ApplicationEvent子类
        return eventClassName.isAssignableFrom(event.getClass());
    }

    /**
     * 简单判断是否是cglib代理的类
     *
     * @param clazz
     * @return
     */
    public boolean isCglibClass(Class<?> clazz) {
        // cn.itnxd.springframework.bean.UserMapper$$EnhancerByCGLIB$$7aa3cb81@33c7e1bb
        if (clazz != null && StrUtil.isNotEmpty(clazz.getName())) {
            return clazz.getName().contains("$$");
        }
        return false;
    }
}
