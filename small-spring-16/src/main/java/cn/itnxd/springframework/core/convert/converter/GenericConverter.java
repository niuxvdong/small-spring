package cn.itnxd.springframework.core.convert.converter;

import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/6/7 22:38
 * @Version 1.0
 * @Description 一般 Converter 接口
 */
public interface GenericConverter {

    // 持有获取类型对的接口
    Set<ConvertiblePair> getConvertibleTypes();

    // 持有核心转换方法的接口
    Object convert(Object source, Class<?> sourceType, Class<?> targetType);

    /**
     * 内部类：保存类型对，源类型与目标类型对
     */
    public static final class ConvertiblePair {

        private final Class<?> sourceType;

        private final Class<?> targetType;

        public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public Class<?> getSourceType() {
            return this.sourceType;
        }

        public Class<?> getTargetType() {
            return this.targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || obj.getClass() != ConvertiblePair.class) {
                return false;
            }
            ConvertiblePair other = (ConvertiblePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }

        @Override
        public int hashCode() {
            return this.sourceType.hashCode() * 31 + this.targetType.hashCode();
        }
    }
}
