package cn.itnxd.springframework.beans;

/**
 * @Author niuxudong
 * @Date 2023/4/11 23:59
 * @Version 1.0
 * @Description 属性值，保存Bean的属性和值的映射关系，都为final类型不可修改
 */
public class PropertyValue {

    private final String name;

    private final Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
