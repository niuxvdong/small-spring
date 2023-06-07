# 第十四章 - 解决代理对象生成后没有继续向下执行的 bug

## 一、问题原因

- 看如下代码实现，会发现如果是代理对象，生成代理对象返回之后会直接短路，不继续执行后面的所有逻辑。
- 因此需要将这个代理对象的生成修改一下执行时机。
- 改到 BeanPostProcessor 的后置处理进行原始 Bean 替换即可

```java
@Override
protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
    Object bean = null;
    try {
        // 增加：判断是否是代理对象(是则直接返回代理对象,不继续走下面流程)
        bean = resolveBeforeInstantiation(beanName, beanDefinition);
        if (bean != null) return bean;

        // 1. 根据 BeanDefinition 创建 Bean
        bean = createBeanInstance(beanName, beanDefinition, args);

        // 增加：实例化之后，设置属性之前通过特殊的 BeanPostProcessor 处理 @value 和 @Autowired 注解的解析
        applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName, bean, beanDefinition);

        // 2. 对 Bean 进行属性填充
        applyPropertyValues(beanName, beanDefinition, bean);
        // 3. bean实例化完成，执行初始化方法以及在初始化前后分别执行BeanPostProcessor
        initializeBean(beanName, beanDefinition, bean);
    } catch (BeansException e) {
        throw new BeansException("初始化Bean失败: ", e);
    }

    // 4. 增加：初始化完成注册实现了销毁接口的对象
    registerDisposableBeanIfNecessary(bean, beanName, beanDefinition);

    // 增加：bean类型判断，单例才添加到单例map中
    if (beanDefinition.isSingleton()) {
        // 5. 添加到单例缓存 map
        addSingleton(beanName, bean);
    }
    return bean;
}
```

## 二、问题解决

### 1、废弃 postProcessBeforeInstantiation 改为 postProcessAfterInitialization 进行代理逻辑融入

- 将 InstantiationAwareBeanPostProcessor 的 postProcessBeforeInstantiation 这个实例化之前进行代理生成的逻辑全部删掉即可
- 为了少一点改动，直接将本接口的两个实现类对该方法的实现，不进行处理返回 bean 或 null 不影响流程即可
- 将 postProcessBeforeInstantiation 的实现逻辑直接搬到 postProcessAfterInitialization 方法中即可
- 简单的改动，查看源代码或查看 github 提交历史

### 2、TargetSource 类获取接口类型兼容 cglib 代理对象

- 将逻辑搬到 postProcessAfterInitialization 之后，方法传入的 bean 实例对象，可能是 cglib 实例化策略生成的对象
- 因此在处理 aop 自动代理时候获取接口类型时就需要增加判断是否是 cglib 对象

```java
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
```

### 3、通过 bean 后置处理在 initializeBean 方法中对原始 bean 进行代理替换

- 删除 createBean 方法中原来进行判断代理对象的逻辑
- 真正的代理替换在 bean 的后置处理器中进行替换
- 因此这里需要用接收一下处理结果，即 `bean = initializeBean(beanName, beanDefinition, bean);`
- 这里解决一个原始代码的问题：
- 在循环中对所有 BeanPostProcessor 的处理时，应该将上一个 BeanPostProcessor 处理的结果传递给下一个 BeanPostProcessor

```java
@Override
protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
    Object bean = null;
    try {
        // 1. 根据 BeanDefinition 创建 Bean
        bean = createBeanInstance(beanName, beanDefinition, args);

        // 增加：实例化之后，设置属性之前通过特殊的 BeanPostProcessor 处理 @value 和 @Autowired 注解的解析
        applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName, bean, beanDefinition);

        // 2. 对 Bean 进行属性填充
        applyPropertyValues(beanName, beanDefinition, bean);
        // 3. bean实例化完成，执行初始化方法以及在初始化前后分别执行BeanPostProcessor
        bean = initializeBean(beanName, beanDefinition, bean);
    } catch (BeansException e) {
        throw new BeansException("初始化Bean失败: ", e);
    }

    // 4. 增加：初始化完成注册实现了销毁接口的对象
    registerDisposableBeanIfNecessary(bean, beanName, beanDefinition);

    // 增加：bean类型判断，单例才添加到单例map中
    if (beanDefinition.isSingleton()) {
        // 5. 添加到单例缓存 map
        addSingleton(beanName, bean);
    }
    return bean;
}
```

## 三、对 aop 代理对象的属性注入测试

### xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>

    <bean id="userService" class="cn.itnxd.springframework.bean.UserServiceImpl">
        <property name="username" value="itnxd"/>
    </bean>

    <bean class="cn.itnxd.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>

    <bean id="beforeAdvice" class="cn.itnxd.springframework.advice.UserServiceBeforeAdvice"/>

    <bean id="methodInterceptor" class="cn.itnxd.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor">
        <property name="advice" ref="beforeAdvice"/>
    </bean>

    <bean id="pointcutAdvisor" class="cn.itnxd.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor">
        <property name="expression" value="execution(* cn.itnxd.springframework.bean.UserService.*(..))"/>
        <property name="advice" ref="methodInterceptor"/>
    </bean>

</beans>
```

### 测试

```java
public class ApiTest {

    @Test
    public void test_autoProxy() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

        UserService userService = applicationContext.getBean("userService", UserService.class);

        userService.getUserInfo();

        /*
        MethodBeforeAdvice 前置方法拦截：getUserInfo
        查询用户信息: itnxd
         */
    }
}
```