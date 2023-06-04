package cn.itnxd.springframework.aop;

/**
 * @Author niuxudong
 * @Date 2023/5/28 17:27
 * @Version 1.0
 * @Description 切点顶层抽象接口
 */
public interface Pointcut {

    /**
     * 切点顶层抽象类有获取类过滤器的方法（匹配类）
     * @return
     */
    ClassFilter getClassFilter();

    /**
     * 也拥有获取方法匹配器的方法（匹配方法）
     * @return
     */
    MethodMatcher getMethodMatcher();
}
