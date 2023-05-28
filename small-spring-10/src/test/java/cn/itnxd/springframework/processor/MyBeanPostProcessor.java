package cn.itnxd.springframework.processor;

import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @Author niuxudong
 * @Date 2023/4/20 23:43
 * @Version 1.0
 * @Description
 */
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 执行初始化之前修改定义好的信息
        if ("userService".equals(beanName)) {
            UserService userService = (UserService) bean;
            userService.setLocation("修改-北京");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
