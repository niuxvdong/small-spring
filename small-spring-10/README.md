# 第十章 - 实现切点表达式以及基于 JDK 和 Cglib 实现 AOP 切面

## 一、切点表达式实现

### 1、顶层切点接口 Pointcut

- 本类具有获取类过滤器 ClassFilter 方法（匹配类）
- 本类也具有获取方法匹配器 MethodMatcher 方法（匹配方法）

```java
public interface Pointcut {

    /**
     * 切点顶层抽象类有获取类过滤器的方法（匹配类）
     * @return
     */
    ClassFilter getClassFilter();

    /**
     * 也拥有获取方法匹配器的方法（匹配方法）
     * @return
     */
    MethodMatcher getMethodMatcher();
}
```

- 类过滤器 ClassFilter 定义

```java
public interface ClassFilter {

    /**
     * 判断类是否匹配切点表达式
     *
     * @param clazz
     * @return
     */
    boolean matches(Class<?> clazz);
}
```

- 方法匹配器 MethodMatcher 定义

```java
public interface MethodMatcher {

    /**
     * 判断方法和目标对象是否匹配
     *
     * @param method
     * @param targetClass
     * @return
     */
    boolean matches(Method method, Class<?> targetClass);
}
```

### 2、切点表达式实现类 AspectJExpressionPointcut

- 实现切点、类过滤器、方法匹配器三个接口
- 这里的方法实现都是调用的依赖 aspecj 的实现

```java
public class AspectJExpressionPointcut implements Pointcut, ClassFilter, MethodMatcher {

    //  PointcutPrimitive
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

    static {
        // "execution(* cn.itnxd.springframework.bean.UserService.*(..))"
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION); // 默认使用 execution 模式
    }

    private final PointcutExpression pointcutExpression;

    public AspectJExpressionPointcut(String expression) {
        // 获取支持指定execution表达式并使用指定类加载器进行解析的切入点解析器
        PointcutParser pointcutParser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(SUPPORTED_PRIMITIVES, this.getClass().getClassLoader());
        // 使用切入点解析器解析 expression 得到解析结果 PointcutExpression
        pointcutExpression = pointcutParser.parsePointcutExpression(expression);
    }

    /**
     * 类是否匹配
     * @param clazz
     * @return
     */
    @Override
    public boolean matches(Class<?> clazz) {
        return pointcutExpression.couldMatchJoinPointsInType(clazz);
    }

    /**
     * 方法是否匹配
     * @param method
     * @param targetClass
     * @return
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 判断切点表达式是否与方法匹配，alwaysMatches 对给定方法的任何调用都匹配（ always, sometimes, or never）
        return pointcutExpression.matchesMethodExecution(method).alwaysMatches();
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }
}
```

### 3、Pointcut 简单测试

```java
@Test
public void test_aop() throws NoSuchMethodException {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut("execution(* cn.itnxd.springframework.bean.UserService.*(..))");
    Class<UserService> clazz = UserService.class;
    Method method = clazz.getDeclaredMethod("getUserInfo");

    System.out.println(pointcut.matches(clazz));

    System.out.println(pointcut.matches(method, clazz));
}
```

## 二、AOP 通知 Advice 实现

### 1、添加 Advice 通知的包装类 AdvisedSupport

- 持有目标对象、方法拦截器、方法匹配器以便于在代理时使用
- 由于都是调 aspecj 代码，本类直接复制了 https://github.com/DerekYRC/mini-spring 源码。添加一定注释便于理解。

```java
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
```

### 2、增加各类通知

- 由于也是调 aspecj 代码，直接复制了 https://github.com/DerekYRC/mini-spring 源码。添加一定注释便于理解。
- 具体代码及注释查看 small-spring-10 相关代码
- Advisor：通知顶层接口，持有一个获取 advice 通知的方法
- BeforeAdvice：前置通知顶层接口
- MethodBeforeAdvice：方法前置通知（这里只实现一个方法前置通知，其他通知类似）
- PointcutAdvisor：切面通知，提供获取切面 Pointcut 的方法
- TargetSource：被代理的目标对象
- AspectJExpressionPointcutAdvisor：aspectJ 表达式的 advisor 通知
  - 实现了 PointcutAdvisor 接口，把切面 pointcut、拦截方法 advice 和具体的拦截表达式包装在一起。
  - 这样就可以在 xml 的配置中定义一个 pointcutAdvisor 切面拦截器了。

## 三、增加 jdk 动态代理实现的切面

### 1、添加顶层 AOP 代理接口 AopProxy

- 持有一个获取代理的方法

```java
public interface AopProxy {

    /**
     * 获取代理方法。
     * @return
     */
    Object getProxy();
}
```

### 3、jdk 动态代理实现

- 实现 AopProxy 和 InvocationHandler 接口
- 实现 InvocationHandler 接口，可以在本类重写 invoke 方法
- 持有 AdvisedSupport 支持类，方便使用
- 若切点表达式可以匹配该方法，则进行方法拦截器拦截执行（方法拦截器的 invoke 逻辑由用户实现（可以在 method.invoke 增加逻辑））
- 否则直接调用方法执行

```java
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    // 持有通知支持类 AdvisedSupport 方便使用
    private final AdvisedSupport advised;

    public JdkDynamicAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    /**
     * 实现获取代理方法
     * @return
     */
    @Override
    public Object getProxy() {
        // 类加载器，目标类的class，InvocationHandler即本类自己this
        return Proxy.newProxyInstance(getClass().getClassLoader(), advised.getTargetSource().getTargetClass(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 判断切点是否支持该方法
        if (advised.getMethodMatcher().matches(method, advised.getTargetSource().getTarget().getClass())) {
            // 拿到方法拦截器
            MethodInterceptor methodInterceptor = advised.getMethodInterceptor();
            // 调用拦截器进行方法拦截（传入方法执行器）
            // 方法拦截器的 invoke 逻辑由用户实现（可以在 method.invoke 增加逻辑）
            return methodInterceptor.invoke(new ReflectiveMethodInvocation(advised.getTargetSource().getTarget(), method, args));
        }
        // 切点不支持则直接执行 invoke
        return method.invoke(advised.getTargetSource().getTarget(), args);
    }
}
```

- 补充方法调用器实现类 ReflectiveMethodInvocation 

```java
public class ReflectiveMethodInvocation implements MethodInvocation {

    // 目标对象
    protected final Object target;

    // 目标方法
    protected final Method method;

    // 构造方法参数
    protected final Object[] arguments;

    public ReflectiveMethodInvocation(Object target, Method method, Object[] arguments) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * 执行方法
     * @return
     * @throws Throwable
     */
    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target, arguments);
    }

    /**
     * 返回目标对象
     * @return
     */
    @Override
    public Object getThis() {
        return target;
    }

    /**
     * 返回目标方法
     * @return
     */
    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }
}
```


## 四、增加 Cglib 动态代理实现的切面

- 同样持有 AdvisedSupport 支持类便于使用
- 由于 cglib 特殊性，方法拦截器得使用 cglib 依赖的拦截器，因此定义一个拦截器便于使用 DynamicAdvisedInterceptor
- 拦截器需要有方法调用器用来执行方法，因此这里定义 CglibMethodInvocation 来处理不同的逻辑
    - 本类也是继承自 ReflectiveMethodInvocation，但是持有一个 cglib 依赖的 MethodProxy，是通过他来进行方法的调用执行的。

```java
public class CglibAopProxy implements AopProxy{

    // 持有通知支持类 AdvisedSupport 方便使用
    private final AdvisedSupport advised;

    public CglibAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        Enhancer enhancer = new Enhancer();
        // 设置目标对象类
        enhancer.setSuperclass(advised.getTargetSource().getTarget().getClass());
        // 设置接口
        enhancer.setInterfaces(advised.getTargetSource().getTargetClass());
        // 设置回调接口（即方法拦截器）
        enhancer.setCallback(new DynamicAdvisedInterceptor(advised));
        return enhancer.create();
    }

    /**
     * 注意此处的MethodInterceptor是cglib中的接口，advised中的MethodInterceptor的AOP联盟中定义的接口，因此定义此类做适配
     */
    private static class DynamicAdvisedInterceptor implements MethodInterceptor {

        // 持有通知支持类 AdvisedSupport 方便使用
        private final AdvisedSupport advised;

        private DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        /**
         * 拦截器的拦截方法
         * @param o
         * @param method
         * @param objects
         * @param methodProxy
         * @return
         * @throws Throwable
         */
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            // 创建 cglib 方法执行器
            CglibMethodInvocation methodInvocation = new CglibMethodInvocation(advised.getTargetSource().getTarget(), method, objects, methodProxy);
            // 切点表达式与方法匹配
            if (advised.getMethodMatcher().matches(method, advised.getTargetSource().getTarget().getClass())) {
                // 匹配成功则调用 方法拦截器 执行方法，传入方法执行器 CglibMethodInvocation
                return advised.getMethodInterceptor().invoke(methodInvocation);
            }
            // 不匹配则继续执行下一个拦截器
            return methodInvocation.proceed();
        }
    }

    /**
     * cglib 方法执行器，继承自 ReflectiveMethodInvocation
     */
    private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

        // 持有一个 cglib 的方法代理
        private final MethodProxy methodProxy;

        public CglibMethodInvocation(Object target, Method method, Object[] arguments, MethodProxy methodProxy) {
            super(target, method, arguments);
            this.methodProxy = methodProxy;
        }

        /**
         * 重写了 ReflectiveMethodInvocation 的 proceed 方法
         * @return
         * @throws Throwable
         */
        @Override
        public Object proceed() throws Throwable {
            // 通过 cglib 提供的方法代理去执行目标方法
            return this.methodProxy.invoke(this.target, this.arguments);
        }
    }
}
```

## 五、jdk 和 cglib 切面的简单测试

### 添加用户实现的拦截器

- 可以在方法调用器前后进行操作
- invocation.proceed() 方法就是调用的 method.invoke(target, arguments) 方法

```java
public class UserServiceInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            // finally 中增加监控信息
            System.out.println("===========监控-开始===========");
            System.out.println("方法名称：" + invocation.getMethod());
            System.out.println("方法耗时：" + (System.currentTimeMillis() - start) + "ms");
            System.out.println("===========监控-结束===========");
        }
    }
}
```

### 简单测试

```java
@Test
public void test_proxy() throws NoSuchMethodException {

    // 目标对象
    UserService userService = new UserServiceImpl();

    AdvisedSupport advisedSupport = new AdvisedSupport();
    // 包装需要代理的目标对象
    advisedSupport.setTargetSource(new TargetSource(userService));
    // 创建用户实现的方法拦截器
    advisedSupport.setMethodInterceptor(new UserServiceInterceptor());
    // 创建方法匹配器
    advisedSupport.setMethodMatcher(new AspectJExpressionPointcut("execution(* cn.itnxd.springframework.bean.UserService.*(..))").getMethodMatcher());

    // 创建 jdk 代理对象
    UserService jdkProxy = (UserService) new JdkDynamicAopProxy(advisedSupport).getProxy();

    // 创建 cglib 代理对象
    UserService cglibProxy = (UserService) new CglibAopProxy(advisedSupport).getProxy();

    jdkProxy.getUserInfo(); // 拦截的类是 UserService 接口

    cglibProxy.getUserInfo(); // 拦截的类是 UserServiceImpl

    /*
    查询用户信息: xxx
    ===========监控-开始===========
    方法名称：public abstract void cn.itnxd.springframework.bean.UserService.getUserInfo()
    方法耗时：4ms
    ===========监控-结束===========

    查询用户信息: xxx
    ===========监控-开始===========
    方法名称：public void cn.itnxd.springframework.bean.UserServiceImpl.getUserInfo()
    方法耗时：17ms
    ===========监控-结束===========
    */
}
```