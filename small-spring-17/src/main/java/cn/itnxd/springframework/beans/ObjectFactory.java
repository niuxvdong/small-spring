package cn.itnxd.springframework.beans;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/6/10 16:53
 * @Version 1.0
 * @Description 顶层对象工厂
 */
public interface ObjectFactory<T> {

    T getObject() throws BeansException;
}