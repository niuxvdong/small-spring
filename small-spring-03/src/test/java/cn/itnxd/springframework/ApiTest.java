package cn.itnxd.springframework;

import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.junit.Test;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:29
 * @Version 1.0
 * @Description
 */
public class ApiTest {

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 注入 BeanDefinition
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class);
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        // 3. 第一次获取 Bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();

        // 4. 第二次获取 Bean from singleton
        UserService userServiceSingle = (UserService) beanFactory.getBean("userService");
        userServiceSingle.getUserInfo();

        System.out.println(userService == userServiceSingle);
    }
}