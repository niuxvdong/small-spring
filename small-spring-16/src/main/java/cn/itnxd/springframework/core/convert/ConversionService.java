package cn.itnxd.springframework.core.convert;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:39
 * @Version 1.0
 * @Description 类型转换体系的核心接口，类型转服服务顶层接口
 */
public interface ConversionService {

    /**
     * 判断是源类型和目标类型能否转换
     * @param sourceType
     * @param targetType
     * @return
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /**
     * 将源类型转换为目标类型返回
     * @param source
     * @param targetType
     * @return
     * @param <T>
     */
    <T> T convert(Object source, Class<T> targetType);
}
