package cn.itnxd.springframework.converter;

import cn.itnxd.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/6/8 22:25
 * @Version 1.0
 * @Description
 */
public class StringToBooleanConverter implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Boolean.class));
    }

    @Override
    public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
        return Boolean.parseBoolean((String) source);
    }
}
