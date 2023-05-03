package cn.itnxd.springframework.event;

import cn.itnxd.springframework.context.ApplicationListener;
import cn.itnxd.springframework.context.event.ContextClosedEvent;

/**
 * @Author niuxudong
 * @Date 2023/5/3 19:09
 * @Version 1.0
 * @Description
 */
public class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("收到事件【"+event.getClass()+"】消息");
    }
}
