package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/5/1 16:39
 * @Version 1.0
 * @Description 初始化Bean接口
 */
public interface InitializingBean {

    /**
     * 在Bean创建完成后，属性注入之后执行初始化方法 invokeInitMethods
     *
     * @throws BeansException
     */
    void afterPropertiesSet() throws BeansException;
}
