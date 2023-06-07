package cn.itnxd.springframework;

import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:29
 * @Version 1.0
 * @Description
 */
public class ApiTest {

    @Test
    public void test_autoProxy() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

        UserService userService = applicationContext.getBean("userService", UserService.class);

        userService.getUserInfo();

        /*
        MethodBeforeAdvice 前置方法拦截：getUserInfo
        查询用户信息: itnxd
         */
    }
}