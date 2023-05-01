package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/5/1 17:28
 * @Version 1.0
 * @Description 销毁 bean 接口
 */
public interface DisposableBean {

    /**
     * 在 Bean 销毁，虚拟机关闭之前进行操作
     * @throws BeansException
     */
    void destroy() throws BeansException;
}
