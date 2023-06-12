package cn.itnxd.springframework.core.convert.support;

import cn.itnxd.springframework.core.convert.converter.Converter;
import cn.itnxd.springframework.core.convert.converter.ConverterFactory;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:30
 * @Version 1.0
 * @Description 实现 String -> Number 的转换工厂
 */
public class StringToNumberConverterFactory implements ConverterFactory<String, Number> {

    /**
     * 对不同的 Number 类型提供不同的转换器
     * @param targetType
     * @return
     * @param <T>
     */
    @Override
    public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToNumber<T>(targetType);
    }

    /**
     * 内部类实现 Converter 接口
     * @param <T>
     */
    private static final class StringToNumber<T extends Number> implements Converter<String, T> {

        private final Class<T> targetType;

        public StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            if (source.length() == 0) {
                return null;
            }

            if (targetType.equals(Integer.class)) {
                return (T) Integer.valueOf(source);
            } else if (targetType.equals(Long.class)) {
                return (T) Long.valueOf(source);
            }
            // 其他数字类型，省略....
            else {
                throw new IllegalArgumentException("不能转换字符串【" + source + "】到目标类型【" + targetType.getName() + "】");
            }
        }
    }
}
