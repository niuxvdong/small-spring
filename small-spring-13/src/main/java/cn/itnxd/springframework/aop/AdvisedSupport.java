package cn.itnxd.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author derekyi
 * @date 2020/12/6 通知支持类（把代理、拦截、匹配的各项属性包装到一个类中，方便在 Proxy 实现类进行使用）
 */
public class AdvisedSupport {

	//是否使用cglib代理
	private boolean proxyTargetClass = false;

	// 被代理的目标对象
	private TargetSource targetSource;

	// 方法拦截器 jar 包提供
	private MethodInterceptor methodInterceptor;

	// 方法匹配器
	private MethodMatcher methodMatcher;

	public boolean isProxyTargetClass() {
		return proxyTargetClass;
	}

	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	public TargetSource getTargetSource() {
		return targetSource;
	}

	public void setTargetSource(TargetSource targetSource) {
		this.targetSource = targetSource;
	}

	public MethodInterceptor getMethodInterceptor() {
		return methodInterceptor;
	}

	public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
		this.methodInterceptor = methodInterceptor;
	}

	public MethodMatcher getMethodMatcher() {
		return methodMatcher;
	}

	public void setMethodMatcher(MethodMatcher methodMatcher) {
		this.methodMatcher = methodMatcher;
	}
}
