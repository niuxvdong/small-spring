package cn.itnxd.springframework.utils;

/**
 * @Author niuxudong
 * @Date 2023/6/5 21:52
 * @Version 1.0
 * @Description 解析字符串的接口
 */
public interface StringValueResolver {

    /**
     * 字符串解析方法
     *
     * @param strVal
     * @return
     */
    String resolveStringValue(String strVal);
}
