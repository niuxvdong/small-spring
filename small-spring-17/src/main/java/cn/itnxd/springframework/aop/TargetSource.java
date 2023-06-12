package cn.itnxd.springframework.aop;

import cn.hutool.core.util.StrUtil;

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

	/**
	 * 将自动代理融入 bean 生命周期，会在实例化之后将 bean 传入以供代理生成。
	 * 因此这里需要判断 target 目标对象是不是 cglib 生成的
	 * @return
	 */
	public Class<?>[] getTargetClass() {
		Class<?> clazz = this.getTarget().getClass();
		clazz = isCglibClass(clazz) ? clazz.getSuperclass() : clazz;
		return clazz.getInterfaces();
	}

	/**
	 * 提供一个获取 bean 真实类型的方法，因为被 cglib 代理的需要特殊处理一下
	 * @return
	 */
	public Class<?> getActualClass() {
		Class<?> clazz = this.getTarget().getClass();
		return isCglibClass(clazz) ? clazz.getSuperclass() : clazz;
	}

	/**
	 * 简单判断是否是cglib代理的类
	 *
	 * @param clazz
	 * @return
	 */
	public boolean isCglibClass(Class<?> clazz) {
		// cn.itnxd.springframework.bean.UserMapper$$EnhancerByCGLIB$$7aa3cb81@33c7e1bb
		if (clazz != null && StrUtil.isNotEmpty(clazz.getName())) {
			return clazz.getName().contains("$$");
		}
		return false;
	}

	public Object getTarget() {
		return this.target;
	}

}
