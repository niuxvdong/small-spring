package cn.itnxd.springframework.context.event;

import cn.itnxd.springframework.context.ApplicationEvent;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:19
 * @Version 1.0
 * @Description 监听容器关闭事件
 */
public class ContextClosedEvent extends ApplicationContextEvent{

    public ContextClosedEvent(ApplicationEvent source) {
        super(source);
    }
}
