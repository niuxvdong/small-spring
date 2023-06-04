# 第十一章 - 把 AOP 切面逻辑融入 Bean 生命周期

## 一、增加通知接口

- BeforeAdvice
- MethodBeforeAdvice：方法前置通知
    - before 方法由用户实现，会在方法拦截器的 invoke 方法中被调用执行，执行时机在方法执行器 invocation.proceed() 之前

```java
public interface MethodBeforeAdvice extends BeforeAdvice {

	void before(Method method, Object[] args, Object target) throws Throwable;
}
```

## 二、拦截器对通知进行处理

- 方法前置拦截器对方法前置通知进行拦截处理
- 持有方法前置通知 MethodBeforeAdvice
- 在拦截方法中先执行通知 advice 的由用户实现的 before 方法，在放行执行 invocation.proceed() 方法

```java
public class MethodBeforeAdviceInterceptor implements MethodInterceptor {

    // 持有 advice 方法前置通知
    private MethodBeforeAdvice advice;

    public MethodBeforeAdviceInterceptor() {
    }

    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 拦截器真正执行方法之前执行通知的前置方法 advice.before
        this.advice.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }

    public MethodBeforeAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(MethodBeforeAdvice advice) {
        this.advice = advice;
    }
}
```

## 三、添加切面包装类 AspectJExpressionPointcutAdvisor

- 本类可以理解为一个包装作用，拥有 aspectj 切面、通知 advice、以及切点表达式，方便使用
- 本类会在 xml 配置中进行注入，注入时候需要添加 advice、expression 两个属性，pointcut 在 get 时候自动创建即可

```java
public class AspectJExpressionPointcutAdvisor implements PointcutAdvisor {

	// 持有 AspectJExpressionPointcut 切面
	private AspectJExpressionPointcut pointcut;

	// 持有通知 advice
	private Advice advice;

	// 持有切点表达式
	private String expression;

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public Pointcut getPointcut() {
		if (pointcut == null) {
			pointcut = new AspectJExpressionPointcut(expression);
		}
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
```

## 四、添加代理工厂处理 jdk 和 cglib 选择问题

- 持有 AdvisedSupport 支持类，便于使用
- 根据 isProxyTargetClass 判断使用 cglib 进行代理还是 jdk 进行代理

```java
public class ProxyFactory {

    private AdvisedSupport advisedSupport;

    public ProxyFactory(AdvisedSupport advisedSupport) {
        this.advisedSupport = advisedSupport;
    }

    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private AopProxy createAopProxy() {
        if (advisedSupport.isProxyTargetClass()) {
            return new CglibAopProxy(advisedSupport);
        }
        return new JdkDynamicAopProxy(advisedSupport);
    }
}
```

## 五、创建特殊的 BeanPostProcessor 来处理将 AOP 切面逻辑通入 Bean 生命周期

### 1、增加特殊的 InstantiationAwareBeanPostProcessor

- 也是一个 BeanPostProcessor，可以在生命周期对本 processor 进行特殊处理
- 增加一个前置处理方法，postProcessBeforeInstantiation，专门处理 aop 逻辑

```java
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor, BeanFactoryAware {

    /**
     * bean 实例化或初始化之前执行（与 postProcessBeforeInitialization 作用一样，传入参数不一样）
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;
}
```

### 2、InstantiationAwareBeanPostProcessor 实现类 DefaultAdvisorAutoProxyCreator

- 本质也是一个 BeanPostProcessor
- 主要实现特出的前置处理方法 postProcessBeforeInstantiation
- 填充 advisedSupport 调用代理工厂 ProxyFactory 进行代理对象的创建
- 设置方法拦截器时，传入的是 advice 通知，可以根据继承关系发现，方法拦截器最顶层也是继承自 advice 接口

```java
public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor {

    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    /**
     * bean 初始化方法执行前 进行处理
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        // 基础 bean (advice/pointcut/advisor) 不进行处理
        if (isInfrastructureClass(beanClass)) {
            return null;
        }

        // 获取容器所有的 aspectj 表达式通知
        Collection<AspectJExpressionPointcutAdvisor> advisors = beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class).values();
        for (AspectJExpressionPointcutAdvisor advisor : advisors) {
            ClassFilter classFilter = advisor.getPointcut().getClassFilter();
            // 类过滤器匹配则进行处理
            if (classFilter.matches(beanClass)) {
                AdvisedSupport advisedSupport = new AdvisedSupport();

                TargetSource targetSource = null;
                try {
                    targetSource = new TargetSource(beanClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new BeansException("创建 bean【" + beanName + "】代理失败");
                }

                // 为 advice 支持类填充需要的信息方便使用
                advisedSupport.setTargetSource(targetSource);
                advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
                // advisor 获取的 advice 就是各类通知，例如我们实现的 MethodBeforeAdvice（用户实现这些接口后）
                advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
                // 默认使用 jdk 动态代理
                advisedSupport.setProxyTargetClass(false);

                // 使用代理工厂进行创建代理对象
                return new ProxyFactory(advisedSupport).getProxy();
            }
        }
        return null;
    }

    /**
     * 判断 bean 是否是基础 bean (advice/pointcut/advisor)
     * @param beanClass
     * @return
     */
    private boolean isInfrastructureClass(Class<?> beanClass) {
        return Advice.class.isAssignableFrom(beanClass)
                || Pointcut.class.isAssignableFrom(beanClass)
                || Advisor.class.isAssignableFrom(beanClass);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

## 五、将 AOP 切面逻辑融入 Bean 生命周期

- 省略无关代码
- createBean 方法最开始时就需要判断是否是代理对象，是则生成代理对象直接返回，不继续走后面逻辑
- 如果有 InstantiationAwareBeanPostProcessor 这个 BeanPostProcessor，则执行特殊的 postProcessBeforeInstantiation 方法来处理 AOP 切面逻辑及代理对象的生成（前置处理）
- 并且会继续执行其他所有 BeanPostProcessor 的后置处理方法
- 否则还是走原来的 createBean 逻辑

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    /**
     * 实现父抽奖类 AbstractBeanFactory 其中一个抽象方法 createBean
     *
     * 添加：创建 Bean 支持入参
     *
     * @param beanName
     * @param beanDefinition
     * @return
     * @throws BeansException
     */
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            // 增加：判断是否是代理对象(是则直接返回代理对象,不继续走下面流程)
            bean = resolveBeforeInstantiation(beanName, beanDefinition);
            if (bean != null) return bean;

            // 1. 根据 BeanDefinition 创建 Bean
            bean = createBeanInstance(beanName, beanDefinition, args);
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

    /**
     * 创建 bean 第一步先进行代理对象判断，是代理对象则执行完标准后置处理后直接返回，不继续走下面流程
     * @param beanName
     * @param beanDefinition
     * @return
     */
    protected Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {
        // 如果有切面则返回处理过后的代理对象，没有切面处理直接返回 null
        Object bean = applyBeanPostProcessorsBeforeInstantiation(beanDefinition.getBeanClass(), beanName);
        if (bean != null) {
            // 代理对象生成之后，执行 BeanPostProcessor 的后置处理方法（前置处理方法由 InstantiationAwareBeanPostProcessor 进行替换处理了）
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        }
        return bean;
    }

    /**
     * 处理 aop 的特殊 processor: InstantiationAwareBeanPostProcessor
     * 返回融入切面的代理对象（cglib或jdk）
     * @param beanClass
     * @param beanName
     * @return
     */
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
        // 获取所有 BeanPostProcessor
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            // 找到实现了接口 InstantiationAwareBeanPostProcessor 的 processor
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                // 执行 InstantiationAwareBeanPostProcessor 专门定义的接口 postProcessBeforeInstantiation（处理aop的通知）
                Object result = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessBeforeInstantiation(beanClass, beanName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
}
```

## 六、AOP 切面简单测试

### 添加 spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>

    <bean id="userService" class="cn.itnxd.springframework.bean.UserServiceImpl"/>

    <!--注入DefaultAdvisorAutoProxyCreator 这个 BeanPostProcessor -->
    <bean class="cn.itnxd.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>

    <!--注入方法前置通知-->
    <bean id="beforeAdvice" class="cn.itnxd.springframework.advice.UserServiceBeforeAdvice"/>

    <!--注入方法前置通知的拦截器-->
    <bean id="methodInterceptor" class="cn.itnxd.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor">
        <property name="advice" ref="beforeAdvice"/>
    </bean>

    <!--注入切面通知 AspectJExpressionPointcutAdvisor -->
    <bean id="pointcutAdvisor" class="cn.itnxd.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor">
        <!--填充表达式-->
        <property name="expression" value="execution(* cn.itnxd.springframework.bean.UserService.*(..))"/>
        <!--填充advice通知-->
        <property name="advice" ref="methodInterceptor"/>
    </bean>
</beans>
```

### 增加方法前置通知

```java
public class UserServiceBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("MethodBeforeAdvice 前置方法拦截：" + method.getName());
    }
}
```

### 测试

```java
@Test
public void test_autoProxy() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

    UserService userService = applicationContext.getBean("userService", UserService.class);

    userService.getUserInfo();
}

MethodBeforeAdvice 前置方法拦截：getUserInfo
查询用户信息: xxx
```