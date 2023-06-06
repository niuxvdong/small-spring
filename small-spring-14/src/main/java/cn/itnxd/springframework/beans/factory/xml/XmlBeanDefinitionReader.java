package cn.itnxd.springframework.beans.factory.xml;

import cn.hutool.core.util.StrUtil;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanReference;
import cn.itnxd.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import cn.itnxd.springframework.beans.factory.support.BeanDefinitionRegistry;
import cn.itnxd.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import cn.itnxd.springframework.core.io.Resource;
import cn.itnxd.springframework.core.io.ResourceLoader;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Author niuxudong
 * @Date 2023/4/13 22:22
 * @Version 1.0
 * @Description
 *
 * BeanDefinitionReader是读取bean定义信息的抽象接口，XmlBeanDefinitionReader是从xml文件中读取的实现类。
 * BeanDefinitionReader需要有获取资源的能力，且读取bean定义信息后需要往容器中注册BeanDefinition，
 * 因此BeanDefinitionReader的抽象实现类AbstractBeanDefinitionReader拥有ResourceLoader和BeanDefinitionRegistry两个属性。
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    /**
     * 单参构造
     * @param registry
     */
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * 两参构造
     * @param registry
     * @param resourceLoader
     */
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        super(registry, resourceLoader);
    }

    /**
     * 实现父接口的location数组加载Bean的方法，即遍历进行调用即可
     * @param locations
     * @throws BeansException
     */
    @Override
    public void loadBeanDefinitions(String[] locations) throws BeansException {
        for (String location : locations) {
            loadBeanDefinitions(location);
        }
    }

    /**
     * 实现location本地资源或文件系统的BeanDefinition信息装载
     * 将location资源转化为Resource资源调用 public void loadBeanDefinitions(Resource resource) 方法即可
     * @param location
     * @throws BeansException
     */
    @Override
    public void loadBeanDefinitions(String location) throws BeansException {
        // 获取到资源加载器 用来根据传入的字符串获取资源（url,classpath,file）
        Resource resource = getResourceLoader().getResource(location);
        loadBeanDefinitions(resource);
    }

    /**
     * 实现Resource资源的BeanDefinition信息装载
     *
     * 其他两种参数的方法重载最终都会走到这里的核心逻辑。
     *
     * @param resource
     * @throws BeansException
     */
    @Override
    public void loadBeanDefinitions(Resource resource) throws BeansException {
        try {
            // 1. 获取到资源的输入流
            try(InputStream is = resource.getInputStream()) {
                // 2. 核心处理逻辑
                doLoadBeanDefinitions(is);
            }
        } catch (IOException | DocumentException e) {
            throw new BeansException("从资源【" + resource + "】解析xml文档异常, e：", e);
        }
    }

    /**
     * 从资源解析并加载BeanDefinition注册到容器核心处理逻辑
     *
     * @param is
     */
    private void doLoadBeanDefinitions(InputStream is) throws DocumentException {
        SAXReader reader = new SAXReader();
        org.dom4j.Document document = reader.read(is);
        Element root = document.getRootElement();

        // 解析 context:component-scan 标签并扫描指定包中的类，提取类信息，组装成BeanDefinition
        Element componentScan = root.element("component-scan");
        if (componentScan != null) {
            String scanPath = componentScan.attributeValue("base-package");
            if (StrUtil.isEmpty(scanPath)) {
                throw new BeansException("base-package 基础扫描包是空的");
            }
            scanPackage(scanPath);
        }

        List<Element> beanList = root.elements("bean");
        for (Element bean : beanList) {
            String beanId = bean.attributeValue("id");
            String beanName = bean.attributeValue("name");
            String className = bean.attributeValue("class");
            String initMethodName = bean.attributeValue("init-method");
            String destroyMethodName = bean.attributeValue("destroy-method");
            String beanScope = bean.attributeValue("scope");

            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BeansException("【" + className + "】不存在");
            }

            // 获取 beanName 信息，优先获取id，空则读取 name，还空则读取类名首字母小写
            beanName = StrUtil.isNotEmpty(beanId) ? beanId : beanName;
            if (StrUtil.isEmpty(beanName)) {
                // getSimpleName 获取的是 a.b.c 中的 c
                beanName = StrUtil.lowerFirst(clazz.getSimpleName());
            }

            BeanDefinition beanDefinition = new BeanDefinition(clazz);
            // 将 init-method 和 destroy-method 属性值进行保存
            beanDefinition.setInitMethodName(initMethodName);
            beanDefinition.setDestroyMethodName(destroyMethodName);
            // 增加bean作用域设置
            if (StrUtil.isNotEmpty(beanScope)) {
                beanDefinition.setScope(beanScope);
            }

            List<Element> propertyList = bean.elements("property");
            for (Element property : propertyList) {
                String propertyNameAttribute = property.attributeValue("name");
                String propertyValueAttribute = property.attributeValue("value");
                String propertyRefAttribute = property.attributeValue("ref");

                if (StrUtil.isEmpty(propertyNameAttribute)) {
                    throw new BeansException("属性名不能为空");
                }

                Object value = propertyValueAttribute;
                if (StrUtil.isNotEmpty(propertyRefAttribute)) {
                    value = new BeanReference(propertyRefAttribute);
                }
                PropertyValue propertyValue = new PropertyValue(propertyNameAttribute, value);
                beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
            }

            if (getRegistry().containsBeanDefinition(beanName)) {
                throw new BeansException("beanName【" + beanName + "】不能重复");
            }
            getRegistry().registerBeanDefinition(beanName, beanDefinition);
        }
    }

    /**
     * 扫描注解Component的类，提取信息，组装成BeanDefinition
     *
     * @param scanPath
     */
    private void scanPackage(String scanPath) {
        String[] basePackages = StrUtil.splitToArray(scanPath, ',');
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(getRegistry());
        scanner.doScan(basePackages);
    }
}
