package cn.itnxd.springframework.aop.framework;

/**
 * @Author niuxudong
 * @Date 2023/5/28 19:31
 * @Version 1.0
 * @Description 顶层AOP代理接口，提供获取代理方法。有 jdk 和 cglib 两种实现。
 */
public interface AopProxy {

    /**
     * 获取代理方法。
     * @return
     */
    Object getProxy();
}
