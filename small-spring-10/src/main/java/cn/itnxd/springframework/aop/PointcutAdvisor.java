package cn.itnxd.springframework.aop;

/**
 * @author derekyi
 * @date 2020/12/6 切点通知
 */
public interface PointcutAdvisor extends Advisor {

	/**
	 * 获取切点方法
	 * @return
	 */
	Pointcut getPointcut();
}
