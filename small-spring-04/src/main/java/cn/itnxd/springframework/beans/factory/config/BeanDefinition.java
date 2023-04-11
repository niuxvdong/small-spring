package cn.itnxd.springframework.beans.factory.config;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:24
 * @Version 1.0
 * @Description BeanDefinition 定义
 */
public class BeanDefinition {

    private Class beanClass;

    public BeanDefinition(Class beanClass){
        this.beanClass = beanClass;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
}
