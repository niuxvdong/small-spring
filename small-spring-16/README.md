# 第十六章 - 通过三级缓存解决循环依赖问题

## 一、无代理对象时通过二级缓存 earlySingletonObjects 解决循环依赖

### 1、产生循环依赖的原因

```
A依赖B，B又依赖A，循环依赖。容器加载时会执行依赖流程：

实例化A，发现依赖B，然后实例化B
实例化B，发现依赖A，然后实例化A
实例化A，发现依赖B，然后实例化B
```

### 2、解决循环依赖的思路

- 解决该问题的关键在于何时将实例化后的 bean 放进容器中
- 若在设置属性完成再将 bean 放入容器就会产生上面的循环依赖问题
- 因此：不能这样做，应该在 bean 实例化后设置属性之前就将 bean 实例进行一下缓存，获取依赖 bean 时去缓存中找，有则返回，这样就不会产生循环依赖问题

执行流程变为：

```
步骤一：getBean(a)，检查singletonObjects是否包含a，singletonObjects不包含a，实例化A放进singletonObjects，设置属性b，发现依赖B，尝试getBean(b)
步骤二：getBean(b)，检查singletonObjects是否包含b，singletonObjects不包含b，实例化B放进singletonObjects，设置属性a，发现依赖A，尝试getBean(a)
步骤三：getBean(a)，检查singletonObjects是否包含a，singletonObjects包含a，返回a
步骤四：步骤二中的b拿到a，设置属性a，然后返回b
步骤五：步骤一中的a拿到b，设置属性b，然后返回a
```

### 3、DefaultSingletonBeanRegistry 增加二级缓存并且修改 getSingleton

- 增加二级缓存
- getSingleton 方法先获取一级缓存是否有完整 bean，即设置了属性之后的 bean
- 没有则获取二级缓存的只实例化未进行设置属性的缓存

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    // 存放单例对象
    // 存放单例对象（一级缓存）
    private final Map<String, Object> singletonObjects = new HashMap<>();

    // 增加二级缓存 earlySingletonObjects
    protected final Map<String, Object> earlySingletonObjects = new HashMap<>();

    // 增加：存放 disposableBean
    private final Map<String, DisposableBean> disposableBeans = new HashMap<>();

    @Override
    public Object getSingleton(String beanName) {
        Object bean = singletonObjects.get(beanName);
        if (bean == null) {
            bean = earlySingletonObjects.get(beanName);
        }
        return bean;
    }
    
    // .................
}
```

### 4、createBean 方法在实例化完成设置属性前增加二级缓存的添加

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            // 1. 根据 BeanDefinition 创建 Bean
            bean = createBeanInstance(beanName, beanDefinition, args);

            // 增加：在 Bean 实例化之后设置属性之前就将 实例放入 二级缓存中来解决循环依赖问题（提前暴露）
            if (beanDefinition.isSingleton()) {
                earlySingletonObjects.put(beanName, bean);
            }

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
            addSingleton(beanName, exposedObject);
        }
        return exposedObject;
    }

    // .......................
}
```

### 5、测试没有代理对象时通过二级缓存解决循环依赖的问题

#### xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	         http://www.springframework.org/schema/beans/spring-beans.xsd
		 http://www.springframework.org/schema/context
		 http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <bean id="a" class="cn.itnxd.springframework.bean.A">
        <property name="b" ref="b"/>
    </bean>

    <bean id="b" class="cn.itnxd.springframework.bean.B">
        <property name="a" ref="a"/>
    </bean>

</beans>
```

#### 测试类

```java
public class ApiTest {

    /**
     * 不涉及代理对象通过二级缓存来解决循环依赖
     * @throws Exception
     */
    @Test
    public void testCircularReference() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");
        A a = applicationContext.getBean("a", A.class);
        B b = applicationContext.getBean("b", B.class);

        System.out.println(a.getB() == b); // true
    }
}
```

## 二、有代理对象时通过三级缓存 singletonFactories 解决循环依赖

### 1、有代理对象产生问题的原因

- 若使用上面的二级缓存解决没有代理对象的依赖问题时是没有问题的
- 但是如果有代理对象，例如依赖的属性是一个被 aop 代理的对象，通过二级缓存只能被注入的是实例化完成的原始 bean，而不是真正被代理的对象

### 2、解决问题的思路

- 增加三级缓存缓存代理对象的引用，当发现依赖的是一个代理对象时，会获取到三级缓存里保存的引用
- 通过该引用获取到真实的代理对象
- 由于保存的是一个引用，只有需要设置该代理对象属性时候，才会调用这个创建代理对象的逻辑（延迟创建）
- 这样就能保住设置的属性就是代理对象

### 3、修复一个使用 cglib 实例化策略获取真实类型的 bug

- 增加：getActualClass 方法用来替换掉 getTarget().getClass() 获取到的 cglib 非实例类型的问题

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
```

- 修改因为 cglib 获取真实类型受到影响的 CglibAopProxy.getProxy 方法

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
        enhancer.setSuperclass(advised.getTargetSource().getActualClass());
        // 设置接口
        enhancer.setInterfaces(advised.getTargetSource().getTargetClass());
        // 设置回调接口（即方法拦截器）
        enhancer.setCallback(new DynamicAdvisedInterceptor(advised));
        return enhancer.create();
    }

    // ................
}
```

### 4、添加代理对象引用 ObjectFactory

- 解决代理对象循环依赖的精华之处
- 使用类似工厂 Bean 的逻辑，现象容器保存这么一个引用，等到真实需要用到进行调用 getObject 时候再进行代理对象的创建（延迟效果）

```java
public interface ObjectFactory<T> {

    T getObject() throws BeansException;
}
```

### 5、InstantiationAwareBeanPostProcessor 增加获取真实代理对象的方法

- 添加默认实现，返回原始 bean 即可

```java
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor, BeanFactoryAware {

    // ................

    /**
     * 若被代理，则提前暴露代理引用（默认实现，具体由子类实现 DefaultAdvisorAutoProxyCreator）
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

### 6、增加 getEarlyBeanReference 实现

- 逻辑是先进行本类持有的 earlyProxyReferences 中进行缓存这个代理对象的 beanName
- 再通过真实创建代理的方法进行创建
- 这里添加 earlyProxyReferences 解决的问题是（可以从调用他的地方发现）：
    - 第一处就是 getEarlyBeanReference 中进行第一次的保存
    - 第二处就是 postProcessAfterInitialization 在 bean 的后置处理器中进行替换原始 bean 时候
        - 即如果在其他 bean 获取代理对象先将标记（beanName）保存到了 earlyProxyReferences 后，则代理对象本身调用 bean 的后置处理器时候调用到这个方法时，就会判断是否已经创建过一次了，创建过直接返回一级缓存的 bean（这个bean就是代理对象，是在其他bean依赖自己时候被保存的）
    - 也就是 earlyProxyReferences 是一个标记，标记是否已经创建过这个代理对象，或者说容器中以及缓存是否已经持有的这个代理对象

```java
public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor {

    private DefaultListableBeanFactory beanFactory;

    // 保存代理对象的引用
    private final Set<Object> earlyProxyReferences = new HashSet<>();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    /**
     * 废弃：这个在实例化前处理的 代理生成，防止返回 代理对象后影响 createBean 流程的后续操作，造成短路
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * 增加：通过 BeanPostProcessor 的后置处理中，修改 bean 实例，替换为代理对象（若有）
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 增加：若有 InstantiationAwareBeanPostProcessor 接口的 getEarlyBeanReference 实现，则这里会进入判断返回 原始 bean
        // 但保存到容器一级缓存的是
        if (earlyProxyReferences.contains(beanName)) {
            return bean;
        }
        return wrapIfNecessary(bean, beanName);
    }

    private Object wrapIfNecessary(Object bean, String beanName) {
        // 基础 bean (advice/pointcut/advisor) 不进行处理
        if (isInfrastructureClass(bean.getClass())) {
            // 修改这里返回原对象而不是 null
            return bean;
        }

        // 获取容器所有的 aspectj 表达式通知
        Collection<AspectJExpressionPointcutAdvisor> advisors = beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class).values();
        for (AspectJExpressionPointcutAdvisor advisor : advisors) {
            ClassFilter classFilter = advisor.getPointcut().getClassFilter();
            // 类过滤器匹配则进行处理
            if (classFilter.matches(bean.getClass())) {
                AdvisedSupport advisedSupport = new AdvisedSupport();

                // 注意：改变自动代理融入时机，则这里的 bean 可能是 cglib 生成的实例，在动态代理获取 interface 时会有问题，
                // 因此在 targetSource 类中做一下判断处理
                TargetSource targetSource = new TargetSource(bean);

                // 为 advice 支持类填充需要的信息方便使用
                advisedSupport.setTargetSource(targetSource);
                advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
                // advisor 获取的 advice 就是各类通知，例如我们实现的 MethodBeforeAdvice（用户实现这些接口后）
                advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
                // 默认使用 cglib 动态代理
                advisedSupport.setProxyTargetClass(true);

                // 使用代理工厂进行创建代理对象
                return new ProxyFactory(advisedSupport).getProxy();
            }
        }
        return bean;
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
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 职责分离，本类只处理 aop 切面代理对象的生成。
     * 本方法直接返回不做处理。
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        return pvs;
    }

    /**
     * 提前暴露代理对象的引用
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        // 先向代理对象引用集合中添加 beanName
        earlyProxyReferences.add(beanName);
        // 再进行代理对象的创建返回
        return wrapIfNecessary(bean, beanName);
    }
}
```

### 7、修改 getSingleton 方法逻辑

- 先获取一级缓存
- 空则获取二级缓存
- 仍空则获取三级缓存（兜底缓存，一定不空）
- 这时通过三级缓存保存的代理对象引用获取真实的代理对象，这时会第一次调用 getObject 方法
- 进而调用 getEarlyBeanReference，向 earlyProxyReferences 添加 标记，并创建 代理对象
- 接下来需要做一些善后操作，即将代理对象扔入二级缓存同时清理掉三级缓存（三级缓存的使命就是生成代理对象，生成完毕就失去意义了）
- 这时一级缓存没有东西，二级缓存保存代理对象，三级缓存已被清空

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    // 存放单例对象（一级缓存）
    private final Map<String, Object> singletonObjects = new HashMap<>();

    // 增加二级缓存 earlySingletonObjects
    private final Map<String, Object> earlySingletonObjects = new HashMap<>();

    // 增加三级缓存 singletonFactories
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();

    // 增加：存放 disposableBean
    private final Map<String, DisposableBean> disposableBeans = new HashMap<>();

    @Override
    public Object getSingleton(String beanName) {
        // 先获取一级缓存（完整对象）
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null) {
            // 为空获取二级缓存（未设置属性的对象）
            singletonObject = earlySingletonObjects.get(beanName);
            if (singletonObject == null) {
                // 仍空则获取三级缓存（代理对象的引用）
                ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
                if (singletonFactory != null) {
                    // 三级缓存不空则获取代理对象引用返回
                    singletonObject = singletonFactory.getObject();
                    // 将三级缓存代理对象引用放进二级缓存
                    earlySingletonObjects.put(beanName, singletonObject);
                    // 删除三级缓存代理对象引用
                    singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    /**
     * 添加三级缓存
     * @param beanName
     * @param singletonFactory
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        singletonFactories.put(beanName, singletonFactory);
    }

    /**
     * 一个 public 方法，保存单例对象到 map 中
     * 可以被继承本类的子类调用，主要包括：AbstractBeanFactory 以及继承的 DefaultListableBeanFactory 调用
     * @param beanName
     * @param singletonObject
     */
    @Override
    public void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
        // 增加：向一级缓存添加对象，需要将二级三级缓存清空
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
    }
}
```

### 8、再次修改 createBean 逻辑

- 这个三级缓存的时机同样是在 bean 实例化之后，属性设置之前（一定得是前，否则死循环）
- 三级缓存的逻辑很简单就是向容器保存一个代理对象引用，即 beanName 和 ObjectFactory 的映射，这个 ObjectFactory 的核心逻辑在传入的 lambda 表达式 getEarlyBeanReference
- 这个会在需要设置这个代理对象属性时才会被第一次调用 getEarlyBeanReference
- 第二处修改，在最后返回 bean 之前，需要将真实的代理对象添加到容器的一级缓存中，添加的逻辑里将 二级缓存 也清空即可
- 此时，容器里二级缓存保存的代理对象被清空，三级也是空的，一级保存的是真实的代理对象
- 若不修改第二处，会导致添加到容器中的一级缓存的是原始 Bean，因为在 bean 的后置处理逻辑中，因为 earlyProxyReferences 集合已经标记过了，是不会再次穿件代理对象返回的，也就是若不修改第二处，这里添加的 Bean 还是原始 Bean，而不是代理对象。
 
```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    // .....................

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            // 1. 根据 BeanDefinition 创建 Bean
            bean = createBeanInstance(beanName, beanDefinition, args);

            // 修改：循环依赖解决
            if (beanDefinition.isSingleton()) {
                Object finalBean = bean;
                // 实例化完成需要先将代理对象引用保存到三级缓存（若有：提前暴露代理对象的引用即可）
                // 将获取到的代理对象引用（类似工厂Bean）保存到 singletonFactories 三级缓存中
                addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, beanDefinition, finalBean));
            }

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

        Object exposedObject = bean;
        // 增加：bean类型判断，单例才添加到单例map中
        if (beanDefinition.isSingleton()) {
            // 若是代理对象获取代理对象
            exposedObject = getSingleton(beanName);
            // 5. 添加到单例缓存 map
            addSingleton(beanName, exposedObject);
        }
        return exposedObject;
    }

    /**
     * 若为代理对象，获取代理对象引用；否则获取原 Bean
     * @param beanName
     * @param beanDefinition
     * @param bean
     * @return
     */
    protected Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object bean) {
        Object exposedObject = bean;
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            // 如果当前 bean 被代理了，即实现了 InstantiationAwareBeanPostProcessor 接口
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                // 则获取到代理对象的引用返回
                exposedObject = ((InstantiationAwareBeanPostProcessor) bp).getEarlyBeanReference(exposedObject, beanName);
                if (exposedObject == null) {
                    return exposedObject;
                }
            }
        }
        return exposedObject;
    }

}
```

### 9、添加三级缓存解决循环依赖的测试

#### xml 配置

- a 被 aop 切面代理
- b 持有 a

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	         http://www.springframework.org/schema/beans/spring-beans.xsd
		 http://www.springframework.org/schema/context
		 http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <!-- a被代理 -->
    <bean id="a" class="cn.itnxd.springframework.bean.A">
        <property name="b" ref="b"/>
    </bean>

    <bean id="b" class="cn.itnxd.springframework.bean.B">
        <property name="a" ref="a"/>
    </bean>

    <!--添加 BeanPostProcessor 处理自动代理 -->
    <bean class="cn.itnxd.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>

    <!--添加切点表达式和通知-->
    <bean id="pointcutAdvisor" class="cn.itnxd.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor">
        <property name="expression" value="execution(* cn.itnxd.springframework.bean.A.fun(..))"/>
        <property name="advice" ref="methodInterceptor"/>
    </bean>

    <!--添加方法拦截器处理通知-->
    <bean id="methodInterceptor" class="cn.itnxd.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor">
        <property name="advice" ref="beforeAdvice"/>
    </bean>

    <!--代理 A 的方法前置通知-->
    <bean id="beforeAdvice" class="cn.itnxd.springframework.advice.ABeforeAdvice"/>

</beans>
```

#### 测试类

- 测试通过，b 持有的的确是 a 的代理对象

```java
public class ApiTest {

    /**
     * 不涉及代理对象通过二级缓存来解决循环依赖
     * @throws Exception
     */
    @Test
    public void testCircularReference() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");
        A a = applicationContext.getBean("a", A.class);
        B b = applicationContext.getBean("b", B.class);

        System.out.println(a.getB() == b); // true

        // a 是代理对象，查看 b 注入的是不是代理对象 a
        System.out.println(b.getA() == a); // true
    }
}
```