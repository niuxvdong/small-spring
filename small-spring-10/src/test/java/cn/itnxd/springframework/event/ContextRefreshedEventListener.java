package cn.itnxd.springframework.event;

import cn.itnxd.springframework.context.ApplicationListener;
import cn.itnxd.springframework.context.event.ApplicationContextEvent;
import cn.itnxd.springframework.context.event.ContextClosedEvent;
import cn.itnxd.springframework.context.event.ContextRefreshedEvent;

/**
 * @Author niuxudong
 * @Date 2023/5/3 19:07
 * @Version 1.0
 * @Description
 */
public class ContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("收到事件【"+event.getClass()+"】消息");
    }
}
