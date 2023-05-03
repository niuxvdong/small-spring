package cn.itnxd.springframework.context.event;

import cn.itnxd.springframework.context.ApplicationEvent;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:17
 * @Version 1.0
 * @Description 继承 ApplicationEvent 的抽象类，其他事件类都继承本类
 */
public abstract class ApplicationContextEvent extends ApplicationEvent {

    public ApplicationContextEvent(ApplicationEvent source) {
        super(source);
    }

    public ApplicationEvent getApplicationContext() {
        return (ApplicationEvent) getSource();
    }
}
