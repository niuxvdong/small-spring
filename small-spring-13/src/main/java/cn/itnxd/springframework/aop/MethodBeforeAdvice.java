package cn.itnxd.springframework.aop;

import java.lang.reflect.Method;

/**
 * @author derekyi
 * @date 2020/12/6 方法前置通知
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

	void before(Method method, Object[] args, Object target) throws Throwable;
}
