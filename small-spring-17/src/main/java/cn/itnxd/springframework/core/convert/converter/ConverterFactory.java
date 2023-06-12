package cn.itnxd.springframework.core.convert.converter;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:28
 * @Version 1.0
 * @Description 类型转换工厂，提供获取转换器的接口
 */
public interface ConverterFactory<S, R> {

    /**
     * 获取转换器方法。
     * 特点：支持一种 S 多种 T, T 只要是 R 或子类即可。例如将 String -> Number
     *
     * @param targetType
     * @return
     * @param <T>
     */
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
