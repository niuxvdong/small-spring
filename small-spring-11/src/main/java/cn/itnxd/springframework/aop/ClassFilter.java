package cn.itnxd.springframework.aop;

/**
 * @Author niuxudong
 * @Date 2023/5/28 17:30
 * @Version 1.0
 * @Description 类过滤器顶层接口
 */
public interface ClassFilter {

    /**
     * 判断类是否匹配切点表达式
     *
     * @param clazz
     * @return
     */
    boolean matches(Class<?> clazz);
}