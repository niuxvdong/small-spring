package cn.itnxd.springframework.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author niuxudong
 * @Date 2023/4/12 0:02
 * @Version 1.0
 * @Description 保存属性值映射关系的集合
 */
public class PropertyValues {

    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    public void addPropertyValue(PropertyValue pv) {
        this.propertyValueList.add(pv);
    }

    /**
     * 将属性集合转化为数组
     * @return
     */
    public PropertyValue[] getPropertyValues() {
        return this.propertyValueList.toArray(new PropertyValue[0]);
    }

    /**
     * 根据属性名获取属性映射pv
     * @param propertyName
     * @return
     */
    public PropertyValue getPropertyValue(String propertyName) {
        return propertyValueList.stream().filter(pv -> pv.getName().equals(propertyName)).findFirst().orElse(null);
    }
}
