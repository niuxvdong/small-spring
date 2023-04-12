package cn.itnxd.springframework;

import cn.itnxd.springframework.bean.UserMapper;
import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.PropertyValues;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanReference;
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

        // 2. 注入依赖 BeanDefinition userMapper
        beanFactory.registerBeanDefinition("userMapper", new BeanDefinition(UserMapper.class));

        // 3. 注入 userService (设置属性id和userMapper)
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("id", "10001"));
        // 属性为 Bean 时，注入的是一个 BeanReference，以便于执行相关注入属性操作进行依赖 Bean的逻辑
        propertyValues.addPropertyValue(new PropertyValue("userMapper", new BeanReference("userMapper")));
        beanFactory.registerBeanDefinition("userService", new BeanDefinition(UserService.class, propertyValues));

        // 4. 获取 Bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();
    }
}