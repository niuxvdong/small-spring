package cn.itnxd.springframework.converter;

import cn.itnxd.springframework.beans.factory.FactoryBean;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/6/8 23:38
 * @Version 1.0
 * @Description
 */
public class ConvertersFactoryBean implements FactoryBean<Set<?>> {

    @Override
    public Set<?> getObject() throws Exception {
        Set<Object> converters = new HashSet<>();
        StringToBooleanConverter stringToBooleanConverter = new StringToBooleanConverter();
        converters.add(stringToBooleanConverter);
        return converters;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}