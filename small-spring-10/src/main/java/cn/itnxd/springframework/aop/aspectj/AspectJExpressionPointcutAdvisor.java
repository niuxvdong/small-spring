package cn.itnxd.springframework.aop.aspectj;

import cn.itnxd.springframework.aop.Pointcut;
import cn.itnxd.springframework.aop.PointcutAdvisor;
import org.aopalliance.aop.Advice;

/**
 * aspectJ 表达式的 advisor 通知
 *
 * @author derekyi
 * @date 2020/12/6
 */
public class AspectJExpressionPointcutAdvisor implements PointcutAdvisor {

	// 持有 AspectJExpressionPointcut 切点
	private AspectJExpressionPointcut pointcut;

	// 持有通知 advice
	private Advice advice;

	// 持有切点表达式
	private String expression;

	public void setExpression(String expression) {
		this.expression = expression;
		pointcut = new AspectJExpressionPointcut(expression);
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}
}
