package cn.itnxd.springframework.beans.factory.config;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:29
 * @Version 1.0
 * @Description 单例Bean顶层接口
 */
public interface SingletonBeanRegistry {

    /**
     * 本接口只有一个方法获取单例Bean
     * @param beanName
     * @return
     */
    Object getSingleton(String beanName);

    void addSingleton(String beanName, Object singletonObject);
}
