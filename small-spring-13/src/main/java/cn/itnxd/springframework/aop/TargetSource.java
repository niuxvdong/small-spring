package cn.itnxd.springframework.aop;

/**
 * 被代理的目标对象
 *
 * @author derekyi
 * @date 2020/12/6
 */
public class TargetSource {

	// 持有目标对象
	private final Object target;

	public TargetSource(Object target) {
		this.target = target;
	}

	public Class<?>[] getTargetClass() {
		return this.target.getClass().getInterfaces();
	}

	public Object getTarget() {
		return this.target;
	}

}
