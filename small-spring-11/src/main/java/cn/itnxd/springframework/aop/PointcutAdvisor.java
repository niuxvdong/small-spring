package cn.itnxd.springframework.aop;

/**
 * @author derekyi
 * @date 2020/12/6 包含一个Pointcut切面和一个Advice的组合，
 * Pointcut切面用于捕获JoinPoint切点，Advice决定在JoinPoint切点执行某种操作。
 * 实现了一个支持aspectj表达式的AspectJExpressionPointcutAdvisor。
 */
public interface PointcutAdvisor extends Advisor {

	/**
	 * 获取切点方法
	 * @return
	 */
	Pointcut getPointcut();
}
