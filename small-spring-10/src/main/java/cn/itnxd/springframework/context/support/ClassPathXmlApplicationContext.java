package cn.itnxd.springframework.context.support;

import cn.itnxd.springframework.beans.exception.BeansException;

/**
 * @Author niuxudong
 * @Date 2023/4/20 22:57
 * @Version 1.0
 * @Description 真正实现ApplicationContext的底层实现类，主要功能是 添加配置文件获取能力，并开启整个容器的refresh流程
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext{

    private String[] configLocations;

    /**
     * 空参构造
     */
    public ClassPathXmlApplicationContext() throws BeansException {
    }

    /**
     * 单配置构造
     * @param configLocation
     */
    public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
        this(new String[]{configLocation});
    }

    /**
     * 多配置构造，传入xml配置路径后，调用refresh开启真个容器刷新流程：
     *      包括：容器创建，加载BeanDefinition到容器，执行BeanFactoryPostProcessor
     *          注册所有的BeanPostProcessor，进行单例bean预实例化（即调用getBean方法）
     *
     * @param configLocations
     */
    public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
        this.configLocations = configLocations;

        // 核心步骤：启动整个容器刷新流程
        refresh();
    }

    @Override
    protected String[] getConfigLocations() {
        return this.configLocations;
    }
}
