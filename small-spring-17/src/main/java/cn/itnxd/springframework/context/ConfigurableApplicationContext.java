package cn.itnxd.springframework.context;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/4/18 21:31
 * @Version 1.0
 * @Description 继承顶层 ApplicationContext, 定义 refresh 接口方法。
 */
public interface ConfigurableApplicationContext extends ApplicationContext{

    /**
     * 刷新容器接口，定义整个容器的执行流程。
     *
     * @throws BeansException
     */
    void refresh() throws BeansException;

    /**
     * 也是虚拟机关闭时候调用，为我们手动调用的方式（手动）
     */
    void close();

    /**
     * 向虚拟机注册钩子方法，在虚拟机关闭时候调用（自动）
     */
    void registerShutdownHook();
}
