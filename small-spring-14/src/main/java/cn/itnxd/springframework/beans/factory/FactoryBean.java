package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/5/3 10:33
 * @Version 1.0
 * @Description 工厂bean接口，实现本接口的bean最终返回拿到的bean不一定是原来的bean，而是本接口的方法getObject方法返回的对象
 */
public interface FactoryBean<T> {

    /**
     * 实现了工厂bean接口最终返回的对象
     *
     * @return
     * @throws BeansException
     */
    T getObject() throws Exception;

    /**
     * 是否单例，单例仍然放入单例池进行缓存
     *
     * @return
     */
    boolean isSingleton();
}
