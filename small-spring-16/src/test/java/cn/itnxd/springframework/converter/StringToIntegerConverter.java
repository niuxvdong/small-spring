package cn.itnxd.springframework.converter;

import cn.itnxd.springframework.core.convert.converter.Converter;

/**
 * @Author niuxudong
 * @Date 2023/6/8 22:21
 * @Version 1.0
 * @Description
 */
public class StringToIntegerConverter implements Converter<String, Integer> {
    @Override
    public Integer convert(String source) {
        return Integer.parseInt(source);
    }
}
