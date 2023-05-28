package cn.itnxd.springframework.event;

import cn.itnxd.springframework.context.ApplicationContext;
import cn.itnxd.springframework.context.event.ApplicationContextEvent;

/**
 * @Author niuxudong
 * @Date 2023/5/3 19:00
 * @Version 1.0
 * @Description 添加自定义事件
 */
public class CustomEvent extends ApplicationContextEvent {

    private String message;

    public CustomEvent(ApplicationContext source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
