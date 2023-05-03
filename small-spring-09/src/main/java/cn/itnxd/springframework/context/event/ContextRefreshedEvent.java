package cn.itnxd.springframework.context.event;

import cn.itnxd.springframework.context.ApplicationContext;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:21
 * @Version 1.0
 * @Description 监听容器refresh刷新事件
 */
public class ContextRefreshedEvent extends ApplicationContextEvent{

    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
}
