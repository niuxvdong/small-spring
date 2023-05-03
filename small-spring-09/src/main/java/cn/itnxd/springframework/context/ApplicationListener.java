package cn.itnxd.springframework.context;

import java.util.EventListener;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:33
 * @Version 1.0
 * @Description 继承了 EventListener 的顶层监听器接口
 *
 * EventListener 是 Java 中用于实现事件监听器的接口,一个实现了 EventListener 接口的类可以注册为事件监听器，
 * 并在事件源触发相应事件时接收并处理事件。
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    /**
     * 定义容器发生事件的监听处理接口
     * @param event 参数是实现了 ApplicationEvent 接口的事件
     */
    void onApplicationEvent(E event);
}
