package cn.itnxd.springframework;

import cn.itnxd.springframework.aop.AdvisedSupport;
import cn.itnxd.springframework.aop.TargetSource;
import cn.itnxd.springframework.aop.aspectj.AspectJExpressionPointcut;
import cn.itnxd.springframework.aop.framework.CglibAopProxy;
import cn.itnxd.springframework.aop.framework.JdkDynamicAopProxy;
import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.bean.UserServiceImpl;
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
    public void test_aop() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut("execution(* cn.itnxd.springframework.bean.UserService.*(..))");
        Class<UserService> clazz = UserService.class;
        Method method = clazz.getDeclaredMethod("getUserInfo");

        System.out.println(pointcut.matches(clazz));

        System.out.println(pointcut.matches(method, clazz));
    }


    @Test
    public void test_proxy() throws NoSuchMethodException {

        // 目标对象
        UserService userService = new UserServiceImpl();

        AdvisedSupport advisedSupport = new AdvisedSupport();
        // 包装需要代理的目标对象
        advisedSupport.setTargetSource(new TargetSource(userService));
        // 创建用户实现的方法拦截器
        advisedSupport.setMethodInterceptor(new UserServiceInterceptor());
        // 创建方法匹配器
        advisedSupport.setMethodMatcher(new AspectJExpressionPointcut("execution(* cn.itnxd.springframework.bean.UserService.*(..))").getMethodMatcher());

        // 创建 jdk 代理对象
        UserService jdkProxy = (UserService) new JdkDynamicAopProxy(advisedSupport).getProxy();

        // 创建 cglib 代理对象
        UserService cglibProxy = (UserService) new CglibAopProxy(advisedSupport).getProxy();

        jdkProxy.getUserInfo(); // 拦截的类是 UserService 接口

        cglibProxy.getUserInfo(); // 拦截的类是 UserServiceImpl

        /*
        查询用户信息: xxx
        ===========监控-开始===========
        方法名称：public abstract void cn.itnxd.springframework.bean.UserService.getUserInfo()
        方法耗时：4ms
        ===========监控-结束===========

        查询用户信息: xxx
        ===========监控-开始===========
        方法名称：public void cn.itnxd.springframework.bean.UserServiceImpl.getUserInfo()
        方法耗时：17ms
        ===========监控-结束===========
        */
    }
}