package cn.itnxd.springframework.beans.factory.config;

import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.PropertyValues;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:24
 * @Version 1.0
 * @Description BeanDefinition 定义
 */
public class BeanDefinition {

    private Class beanClass;

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
}
