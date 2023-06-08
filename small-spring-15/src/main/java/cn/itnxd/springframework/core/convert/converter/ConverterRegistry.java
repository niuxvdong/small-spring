package cn.itnxd.springframework.core.convert.converter;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:54
 * @Version 1.0
 * @Description converter 转换器注册中心顶层接口
 */
public interface ConverterRegistry {

    /**
     * 添加转化器
     * @param converter
     */
    void addConverter(Converter<?, ?> converter);

    /**
     * 添加转换工厂
     * @param converterFactory
     */
    void addConverterFactory(ConverterFactory<?, ?> converterFactory);

    /**
     * 添加 GenericConverter 转换器
     * @param converter
     */
    void addConverter(GenericConverter converter);
}
