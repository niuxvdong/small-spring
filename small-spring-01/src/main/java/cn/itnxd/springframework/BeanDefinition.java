package cn.itnxd.springframework;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:21
 * @Version 1.0
 * @Description
 */
public class BeanDefinition {

    private Object bean;

    public BeanDefinition(Object bean) {
        this.bean = bean;
    }

    public Object getBean() {
        return bean;
    }
}
