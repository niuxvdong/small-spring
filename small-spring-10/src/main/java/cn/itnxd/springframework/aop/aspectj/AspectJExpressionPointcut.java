package cn.itnxd.springframework.aop.aspectj;

import cn.itnxd.springframework.aop.ClassFilter;
import cn.itnxd.springframework.aop.MethodMatcher;
import cn.itnxd.springframework.aop.Pointcut;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/5/28 17:36
 * @Version 1.0
 * @Description 切点表达式实现类，实现切点，类过滤器，方法匹配器
 */
public class AspectJExpressionPointcut implements Pointcut, ClassFilter, MethodMatcher {

    //  PointcutPrimitive
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION); // 默认使用 execution 模式
    }

    private final PointcutExpression pointcutExpression;

    public AspectJExpressionPointcut(String expression) {
        // 获取支持指定execution表达式并使用指定类加载器进行解析的切入点解析器
        PointcutParser pointcutParser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(SUPPORTED_PRIMITIVES, this.getClass().getClassLoader());
        // 使用切入点解析器解析 expression 得到解析结果 PointcutExpression
        pointcutExpression = pointcutParser.parsePointcutExpression(expression);
    }

    /**
     * 类是否匹配
     * @param clazz
     * @return
     */
    @Override
    public boolean matches(Class<?> clazz) {
        return pointcutExpression.couldMatchJoinPointsInType(clazz);
    }

    /**
     * 方法是否匹配
     * @param method
     * @param targetClass
     * @return
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 判断切点表达式是否与方法匹配，alwaysMatches 对给定方法的任何调用都匹配（ always, sometimes, or never）
        return pointcutExpression.matchesMethodExecution(method).alwaysMatches();
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }
}
