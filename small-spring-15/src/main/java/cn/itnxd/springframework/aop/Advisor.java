package cn.itnxd.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * @author derekyi
 * @date 2020/12/6 Advisor 通知顶层接口
 */
public interface Advisor {

	/**
	 * 持有一个获取通知的方法 Advice 为 jar 包实现
	 * @return
	 */
	Advice getAdvice();
}
