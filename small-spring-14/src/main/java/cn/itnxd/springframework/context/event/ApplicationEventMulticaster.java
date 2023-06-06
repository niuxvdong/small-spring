package cn.itnxd.springframework.context.event;

import cn.itnxd.springframework.context.ApplicationEvent;
import cn.itnxd.springframework.context.ApplicationListener;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:26
 * @Version 1.0
 * @Description 事件广播器：注册监听器和发布事件的抽象接口
 */
public interface ApplicationEventMulticaster {

    /**
     * 注册监听器
     * @param listener
     */
    void addApplicationListener(ApplicationListener<?> listener);

    /**
     * 移除监听器
     * @param listener
     */
    void removeApplicationListener(ApplicationListener<?> listener);

    /**
     * 事件广播接口
     * @param event
     */
    void multicastEvent(ApplicationEvent event);
}
