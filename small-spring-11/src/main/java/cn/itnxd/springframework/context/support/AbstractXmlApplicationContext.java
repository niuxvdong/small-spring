package cn.itnxd.springframework.context.support;

import cn.itnxd.springframework.beans.factory.support.DefaultListableBeanFactory;
import cn.itnxd.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * @Author niuxudong
 * @Date 2023/4/20 22:43
 * @Version 1.0
 * @Description 主要实现父类 AbstractRefreshableApplicationContext 定义的加载BeanDefinition信息方法
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext{

    /**
     * 解析配置得到BeanDefinition信息注册到容器中
     *
     * @param beanFactory
     */
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        // 传入BeanDefinitionRegistry 和 ResourceLoader
        // AbstractRefreshableApplicationContext继承AbstractApplicationContext继承DefaultResourceLoader
        // 1. 创建BeanDefinition读取器
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        // 2. 获取配置文件路径地址，由子类实现
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            // 3. 交给读取器获取到Resource进而获取到InputStream进而解析流得到BeanDefinition注册到容器中（DefaultListableBeanFactory）
            beanDefinitionReader.loadBeanDefinitions(configLocations);
        }
    }

    /**
     * 获取配置路径，由子类实现
     *
     * @return
     */
    protected abstract String[] getConfigLocations();
}
