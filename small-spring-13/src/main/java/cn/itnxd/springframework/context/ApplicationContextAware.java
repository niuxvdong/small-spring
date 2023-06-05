package cn.itnxd.springframework.context;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.Aware;

/**
 * @Author niuxudong
 * @Date 2023/5/2 23:05
 * @Version 1.0
 * @Description 实现此接口，能感知到所属的 ApplicationContext
 */
public interface ApplicationContextAware extends Aware {

    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
