package cn.itnxd.springframework;

import cn.hutool.core.io.IoUtil;
import cn.itnxd.springframework.bean.UserMapper;
import cn.itnxd.springframework.bean.UserService;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.PropertyValues;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanReference;
import cn.itnxd.springframework.beans.factory.support.DefaultListableBeanFactory;
import cn.itnxd.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import cn.itnxd.springframework.context.support.ClassPathXmlApplicationContext;
import cn.itnxd.springframework.core.io.DefaultResourceLoader;
import cn.itnxd.springframework.core.io.Resource;
import cn.itnxd.springframework.processor.MyBeanFactoryPostProcessor;
import cn.itnxd.springframework.processor.MyBeanPostProcessor;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:29
 * @Version 1.0
 * @Description
 */
public class ApiTest {

    @Test
    public void test_resource() throws IOException {
        // classpath 文件
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:hello.txt");
        String res = IoUtil.readUtf8(resource.getInputStream());
        System.out.println(res);

        // url 流
        Resource urlResource = resourceLoader.getResource("https://www.baidu.com");
        String urlRes = IoUtil.readUtf8(urlResource.getInputStream());
        System.out.println(urlRes);

        // 本地文件流
        Resource fileResource = resourceLoader.getResource("src/main/resources/hello.txt");
        String fileRes = IoUtil.readUtf8(fileResource.getInputStream());
        System.out.println(fileRes);
    }

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 解析xml，注册bean
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions("classpath:spring.xml");

        // 3. 手动注册BeanFactoryProcessor
        MyBeanFactoryPostProcessor beanFactoryPostProcessor = new MyBeanFactoryPostProcessor();
        MyBeanPostProcessor beanPostProcessor = new MyBeanPostProcessor();
        // 实例化之前执行
        beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);

        // 4. 注册BeanPostProcessor，初始化前后执行
        beanFactory.addBeanPostProcessor(beanPostProcessor);

        // 5. 获取bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();

        System.out.println(userService);
    }

    @Test
    public void test_applicationContext() {
        // 1. 创建 ApplicationContext （构造器内触发refresh流程）
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

        // 增加：手动调用注册 shutdownHook 到 runtime（这一步应该做成自动的）
        //applicationContext.registerShutdownHook();

        // 2. 获取 bean
        UserService userService = applicationContext.getBean("userService", UserService.class);

        UserService userService1 = applicationContext.getBean("userService", UserService.class);

        System.out.println(userService == userService1); // false;
//        userService.getUserInfo();
//        System.out.println(userService);
//
//        // 获取aware接口感知到的容器对象
//        System.out.println("getApplicationContext: " + userService.getApplicationContext());
//        System.out.println("getBeanFactory: " + userService.getBeanFactory());
//
//        // 或者：手动调用 close 方法
//        applicationContext.close();
    }
}