package cn.itnxd.springframework.core.convert.support;

import cn.hutool.core.convert.BasicType;
import cn.itnxd.springframework.core.convert.ConversionService;
import cn.itnxd.springframework.core.convert.converter.Converter;
import cn.itnxd.springframework.core.convert.converter.ConverterFactory;
import cn.itnxd.springframework.core.convert.converter.ConverterRegistry;
import cn.itnxd.springframework.core.convert.converter.GenericConverter;
import cn.itnxd.springframework.core.convert.converter.GenericConverter.ConvertiblePair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:42
 * @Version 1.0
 * @Description 类型转换服务的实现类，实现 转换器注册中心和转换服务两个接口
 */
public class GenericConversionService implements ConversionService, ConverterRegistry {

    // 持有转换类型对 ConvertiblePair 和 一般转换器 GenericConverter 的映射集合
    // 可以保存 Converter（通过adapter实现GenericConverter接口）、ConverterFactory（通过adapter实现GenericConverter接口）、GenericConverter 三种
    private final Map<ConvertiblePair, GenericConverter> converters = new HashMap<>();

    /**
     * 实现 ConversionService 的判断接口
     * @param sourceType
     * @param targetType
     * @return
     */
    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        // 获取源类型到目标类型的转换器
        GenericConverter converter = getConverter(sourceType, targetType);
        // 若有这种转换器则说明可以转换
        return converter != null;
    }

    /**
     * 实现 ConversionService 的核心转换接口
     * @param source
     * @param targetType
     * @return
     * @param <T>
     */
    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        Class<?> sourceType = source.getClass();
        // 将目标类型转换成包装类型
        targetType = (Class<T>) BasicType.wrap(targetType);
        // 获取转换器
        GenericConverter converter = getConverter(sourceType, targetType);
        // 使用转换器进行转换
        return (T) converter.convert(source, sourceType, targetType);
    }

    /**
     * 实现 ConverterRegistry 的添加转换器接口
     * @param converter
     */
    @Override
    public void addConverter(Converter<?, ?> converter) {
        // 根据转换器泛型接口获取转换类型对
        ConvertiblePair typeInfo = getRequiredTypeInfo(converter);
        // 通过转换类型对和转换器获取转换器适配器 adapter
        ConverterAdapter converterAdapter = new ConverterAdapter(typeInfo, converter);
        for (ConvertiblePair convertibleType : converterAdapter.getConvertibleTypes()) {
            // 遍历适配器保存的类型转换对，保存到 转换对和 GenericConverter 映射集合中
            // 这里的适配器是 GenericConverter 的实现类
            converters.put(convertibleType, converterAdapter);
        }
    }

    /**
     * 实现 ConverterRegistry 的添加转换器工厂接口
     * @param converterFactory
     */
    @Override
    public void addConverterFactory(ConverterFactory<?, ?> converterFactory) {
        // 根据转换器工厂泛型接口获取转换类型对
        ConvertiblePair typeInfo = getRequiredTypeInfo(converterFactory);
        // 通过转换类型对和转换器工厂获取转换器工厂适配器 adapter
        ConverterFactoryAdapter converterFactoryAdapter = new ConverterFactoryAdapter(typeInfo, converterFactory);
        for (ConvertiblePair convertibleType : converterFactoryAdapter.getConvertibleTypes()) {
            // 遍历适配器保存的类型转换对，保存到 转换对和 GenericConverter 映射集合中
            // 这里的适配器也是 GenericConverter 的实现类
            converters.put(convertibleType, converterFactoryAdapter);
        }
    }

    /**
     * 实现 ConverterRegistry 的添加一般转换器接口
     * @param converter
     */
    @Override
    public void addConverter(GenericConverter converter) {
        for (ConvertiblePair convertibleType : converter.getConvertibleTypes()) {
            // 参数是 GenericConverter 顶层接口，直接添加
            converters.put(convertibleType, converter);
        }
    }

    /**
     * 根据转换器获取转换类型对
     * @param object
     * @return
     */
    private ConvertiblePair getRequiredTypeInfo(Object object) {
        // 获取泛型参数
        Type[] types = object.getClass().getGenericInterfaces();
        ParameterizedType parameterized = (ParameterizedType) types[0];
        Type[] actualTypeArguments = parameterized.getActualTypeArguments();
        // 第一个泛型参数和第二个泛型参数
        Class<?> sourceType = (Class<?>) actualTypeArguments[0];
        Class<?> targetType = (Class<?>) actualTypeArguments[1];
        // 根据两个泛型参数创建转换对返回
        return new ConvertiblePair(sourceType, targetType);
    }

    /**
     * 获取源类型到目的类型的一般转换器
     * @param sourceType
     * @param targetType
     * @return
     */
    protected GenericConverter getConverter(Class<?> sourceType, Class<?> targetType) {
        // 如果是基本类型返回对应的包装类型，包括包装类型的父类，例如 int -> Integer -> Number
        List<Class<?>> sourceCandidates = getClassHierarchy(sourceType);
        List<Class<?>> targetCandidates = getClassHierarchy(targetType);
        for (Class<?> sourceCandidate : sourceCandidates) {
            for (Class<?> targetCandidate : targetCandidates) {
                ConvertiblePair convertiblePair = new ConvertiblePair(sourceCandidate, targetCandidate);
                // 两层循环进行排列组合判断 converter 集合是由有这种转换器，有则返回
                GenericConverter converter = converters.get(convertiblePair);
                if (converter != null) {
                    return converter;
                }
            }
        }
        return null;
    }

    /**
     * 获取传入类型的包装类型 int -> integer，由于有循环因此最终获取到的是 integer、Number
     * @param clazz
     * @return
     */
    private List<Class<?>> getClassHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        clazz = BasicType.wrap(clazz);
        while (clazz != null) {
            hierarchy.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return hierarchy;
    }

    /**
     * 转换器适配器，实现 GenericConverter 接口。可以被保存到本类持有的 converters 集合
     * 包装转换对和转换器映射。
     */
    private static final class ConverterAdapter implements GenericConverter {

        // 持有转换类型对
        private final ConvertiblePair typeInfo;

        // 持有转换器
        private final Converter<Object, Object> converter;

        public ConverterAdapter(ConvertiblePair typeInfo, Converter<?, ?> converter) {
            this.typeInfo = typeInfo;
            this.converter = (Converter<Object, Object>) converter;
        }

        /**
         * 实现获取父类转换对接口
         * @return
         */
        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(typeInfo);
        }

        /**
         * 实现父类核心转换方法
         * @param source
         * @param sourceType
         * @param targetType
         * @return
         */
        @Override
        public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
            return converter.convert(source);
        }
    }

    /**
     * 转换器工厂适配器，实现 GenericConverter 接口。可以被保存到本类持有的 converters 集合
     * 包装转换对和转换工厂映射。
     */
    private static final class ConverterFactoryAdapter implements GenericConverter {

        // 持有转换类型对
        private final ConvertiblePair typeInfo;

        // 持有转换工厂
        private final ConverterFactory<Object, Object> converterFactory;

        public ConverterFactoryAdapter(ConvertiblePair typeInfo, ConverterFactory<?, ?> converterFactory) {
            this.typeInfo = typeInfo;
            this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(typeInfo);
        }

        @Override
        public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
            return converterFactory.getConverter(targetType).convert(source);
        }
    }
}
