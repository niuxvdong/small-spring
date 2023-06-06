package cn.itnxd.springframework;

import cn.itnxd.springframework.aop.AdvisedSupport;
import cn.itnxd.springframework.aop.TargetSource;
import cn.itnxd.springframework.aop.aspectj.AspectJExpressionPointcut;
import cn.itnxd.springframework.aop.framework.CglibAopProxy;
import cn.itnxd.springframework.aop.framework.JdkDynamicAopProxy;
import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.bean.UserServiceImpl;
import cn.itnxd.springframework.context.support.ClassPathXmlApplicationContext;
import cn.itnxd.springframework.interceptor.UserServiceInterceptor;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:29
 * @Version 1.0
 * @Description
 */
public class ApiTest {

    @Test
    public void test_scan() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");

        UserService userService = applicationContext.getBean("userService", UserService.class);

        userService.getUserInfo();

        /*
        查询用户信息: itnxd
        car: cn.itnxd.springframework.bean.Car$$EnhancerByCGLIB$$aaaf2a2f@51efea79
        */
    }
}