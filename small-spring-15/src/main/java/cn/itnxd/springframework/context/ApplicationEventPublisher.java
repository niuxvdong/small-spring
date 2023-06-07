package cn.itnxd.springframework.context;

/**
 * @Author niuxudong
 * @Date 2023/5/3 17:46
 * @Version 1.0
 * @Description 事件发布者顶层接口，所有事件都要从这里发布出去
 */
public interface ApplicationEventPublisher {

    void publishEvent(ApplicationEvent event);
}
