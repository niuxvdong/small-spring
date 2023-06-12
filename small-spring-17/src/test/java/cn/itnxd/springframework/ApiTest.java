package cn.itnxd.springframework;

import cn.itnxd.springframework.bean.A;
import cn.itnxd.springframework.bean.B;
import cn.itnxd.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:29
 * @Version 1.0
 * @Description
 */
public class ApiTest {

    /**
     * 不涉及代理对象通过二级缓存来解决循环依赖
     * @throws Exception
     */
    @Test
    public void testCircularReference() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");
        A a = applicationContext.getBean("a", A.class);
        B b = applicationContext.getBean("b", B.class);

        System.out.println(a.getB() == b); // true

        // a 是代理对象，查看 b 注入的是不是代理对象 a
        System.out.println(b.getA() == a); // true

        a.fun();
    }
}