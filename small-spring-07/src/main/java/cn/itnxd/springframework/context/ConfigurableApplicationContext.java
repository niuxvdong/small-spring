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
}
