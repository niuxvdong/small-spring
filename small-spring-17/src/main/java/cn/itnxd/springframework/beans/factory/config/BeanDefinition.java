package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.PropertyValues;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:24
 * @Version 1.0
 * @Description BeanDefinition 定义
 */
public class BeanDefinition {

    private Class beanClass;

    // 增加bean类型
    public static final String SCOPE_SINGLETON = "singleton";
    public static final String SCOPE_PROTOTYPE = "prototype";

    // 默认单例
    private String scope = SCOPE_SINGLETON;

    private boolean singleton = true;
    private boolean prototype = false;

    // 增加：懒加载，默认不开启
    private boolean lazyInit = false;

    // 增加：初始化方法名称
    private String initMethodName;

    /**
     * 增加：销毁方法名称
     *
     * 这两个属性用来保存 spring.xml 配置的 init-method="xxx" destroy-method="xxx" 中的value，以便于反射调用。
     *
     * 还有一种是直接实现 initializingBean 和 disposableBean 接口 注册到容器 中，进行接口方式的调用，
     */
    private String destroyMethodName;

    // 添加：PropertyValue 属性映射集合
    private PropertyValues propertyValues;

    public BeanDefinition(Class beanClass){
        this.beanClass = beanClass;
        this.propertyValues = new PropertyValues();
    }

    /**
     * BeanDefinition 构造函数增加 PV属性值集合
     *
     * 赋值时判断是否为空进行操作，使属性 propertyValues 一定非空。
     *
     * @param beanClass
     * @param propertyValues
     */
    public BeanDefinition(Class beanClass, PropertyValues propertyValues){
        this.beanClass = beanClass;
        this.propertyValues = propertyValues == null ? new PropertyValues() : propertyValues;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }


    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public boolean isPrototype() {
        return prototype;
    }

    public void setScope(String scope) {
        this.scope = scope;
        this.singleton = SCOPE_SINGLETON.equals(scope);
        this.prototype = SCOPE_PROTOTYPE.equals(scope);
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
}
