package cn.itnxd.springframework.context;

import java.util.EventObject;

/**
 * @Author niuxudong
 * @Date 2023/5/3 16:08
 * @Version 1.0
 * @Description 所有事件都会继承本类
 *
 * EventObject 是 Java 事件模型中的一个核心类之一。它定义了事件对象的基本结构和行为，用于在事件源和事件监听器之间传递事件信息。
 *
 * 在 Java 的事件模型中，事件源是产生事件的对象，而事件监听器则是用于监听事件的对象。当事件源发生某个事件时，它会创建一个事件对象，
 * 并将该事件对象传递给所有注册的事件监听器。事件监听器可以通过该事件对象获取事件的详细信息，并采取相应的处理措施。
 */
public abstract class ApplicationEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public ApplicationEvent(Object source) {
        super(source);
    }
}
