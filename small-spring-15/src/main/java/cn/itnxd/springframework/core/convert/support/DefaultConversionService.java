package cn.itnxd.springframework.core.convert.support;

import cn.itnxd.springframework.core.convert.converter.ConverterRegistry;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:42
 * @Version 1.0
 * @Description 最底层的转换服务实现类（添加默认的转换工厂）
 */
public class DefaultConversionService extends GenericConversionService{

    public DefaultConversionService() {
        // 父类继承自注册中心 ConverterRegistry
        addDefaultConverters(this);
    }

    /**
     * 本类为静态方法，不能直接调用添加转换工厂方法，但可以通过转入转换器注册中心来调用
     * @param converterRegistry
     */
    public static void addDefaultConverters(ConverterRegistry converterRegistry) {
        // 默认转换工厂
        converterRegistry.addConverterFactory(new StringToNumberConverterFactory());
        // 其他默认转换工厂....
    }
}
