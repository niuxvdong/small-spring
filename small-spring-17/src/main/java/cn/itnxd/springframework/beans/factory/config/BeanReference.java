package cn.itnxd.springframework.beans.factory.config;

/**
 * @Author niuxudong
 * @Date 2023/4/12 21:55
 * @Version 1.0
 * @Description Bean 引用信息，保存 Bean 的属性为 Bean 时的 BeanName
 */
public class BeanReference {

    // 使用final修饰防止被更改
    private final String beanName;

    public BeanReference(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
