package cn.itnxd.springframework.event;

import cn.itnxd.springframework.context.ApplicationListener;

/**
 * @Author niuxudong
 * @Date 2023/5/3 19:02
 * @Version 1.0
 * @Description 添加自定义事件的监听器
 */
public class CustomEventListener implements ApplicationListener<CustomEvent> {

    @Override
    public void onApplicationEvent(CustomEvent event) {
        System.out.println("收到事件【"+event.getClass()+"】消息：" + event.getMessage());
    }
}
