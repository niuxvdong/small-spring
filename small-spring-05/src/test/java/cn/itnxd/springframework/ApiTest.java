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
import cn.itnxd.springframework.core.io.DefaultResourceLoader;
import cn.itnxd.springframework.core.io.Resource;
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

        // 2. 解析xml
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions("classpath:spring.xml");

        // 3. 获取bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();
    }
}