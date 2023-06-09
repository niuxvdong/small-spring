package cn.itnxd.springframework.context.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.FactoryBean;
import cn.itnxd.springframework.beans.factory.InitializingBean;
import cn.itnxd.springframework.core.convert.ConversionService;
import cn.itnxd.springframework.core.convert.converter.Converter;
import cn.itnxd.springframework.core.convert.converter.ConverterFactory;
import cn.itnxd.springframework.core.convert.converter.ConverterRegistry;
import cn.itnxd.springframework.core.convert.converter.GenericConverter;
import cn.itnxd.springframework.core.convert.support.DefaultConversionService;
import cn.itnxd.springframework.core.convert.support.GenericConversionService;

import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/6/8 22:46
 * @Version 1.0
 * @Description 类型转换服务，可以在 xml 中配置注入，beanName 为 conversionService
 */
public class ConversionServiceFactoryBean implements FactoryBean<ConversionService>, InitializingBean {

    // 持有类型转换器集合（Converter、ConverterFactory、GenericConverter）
    private Set<?> converters;

    // 持有 ConversionService 转换服务的实现类
    private GenericConversionService conversionService;

    /**
     * 实现 FactoryBean 的 getObj 方法
     * @return
     * @throws Exception
     */
    @Override
    public ConversionService getObject() throws Exception {
        return this.conversionService;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * set 方法，在属性赋值时期进行设置 converters
     * @param converters
     */
    public void setConverters(Set<?> converters) {
        this.converters = converters;
    }

    /**
     * 实现 InitializingBean 接口方法（bean创建完成属性设置完属性之后进行初始化方法执行initializeBean）
     *
     * 初始化方法中进行设置
     * @throws BeansException
     */
    @Override
    public void afterPropertiesSet() throws BeansException {
        // 初始化方法中为持有的属性 转换服务 赋值为 DefaultConversionService（自动添加默认的转换器）
        conversionService = new DefaultConversionService();
        // 注册属性赋值过程注册的 converters 到 conversionService中
        registerConverters(converters, conversionService);
    }

    /**
     * 将设置属性时期添加的 converters 保存到 converter 注册中心中
     * @param converters
     * @param converterRegistry
     */
    private void registerConverters(Set<?> converters, ConverterRegistry converterRegistry) {
        if (converters != null) {
            for (Object converter : converters) {
                if (converter instanceof GenericConverter) {
                    converterRegistry.addConverter((GenericConverter) converter);
                } else if (converter instanceof Converter<?, ?>) {
                    converterRegistry.addConverter((Converter<?, ?>) converter);
                } else if (converter instanceof ConverterFactory<?, ?>) {
                    converterRegistry.addConverterFactory((ConverterFactory<?, ?>) converter);
                } else {
                    throw new IllegalArgumentException("类型转换器 converter 必须实现以下三大接口：Converter, ConverterFactory, GenericConverter");
                }
            }
        }
    }
}
