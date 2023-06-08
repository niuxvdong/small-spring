package cn.itnxd.springframework.core.convert.converter;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:26
 * @Version 1.0
 * @Description 顶层类型转换接口 S 转换成 T
 */
public interface Converter<S, T> {

    /**
     * 类型转换 S -> T
     */
    T convert(S source);
}
