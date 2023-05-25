# 第八章 - 实现 Bean 作用域以及 FactoryBean （二）

## 一、实现 prototype 多例 Bean 支持

### 1、BeanDefinition 中增加 bean 类型字段

- BeanDefinition 定义信息增加 scope 字段标记 bean 类型
- 添加判断 bean 类型的方法 isSingleton
- 添加设置 bean 类型的方法 setScope

```java
// 增加bean类型
public static final String SCOPE_SINGLETON = "singleton";
public static final String SCOPE_PROTOTYPE = "prototype";

// 默认单例
private String scope = SCOPE_SINGLETON;

private boolean singleton = true;
private boolean prototype = false;


public boolean isSingleton() {
    return singleton;
}

public boolean isPrototype() {
    return prototype;
}

public void setScope(String scope) {
    this.scope = scope;
    this.singleton = SCOPE_SINGLETON.equals(scope);
    this.prototype = SCOPE_PROTOTYPE.equals(scope);
}
```

### 2、createBean 方法增加 单例 Bean 逻辑

- 只有是单例 Bean 才添加到 DefaultSingletonBeanRegistry 持有的 singletonObjects map 集合中

```java
@Override
protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
    Object bean = null;
    try {
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
```

### 3、修改注册销毁接口方法

- 单例才进行销毁接口的注册
- 即多例 bean 不需要有销毁方法
- prototype 类型的 bean 每次请求都是一个新创建的，没有向单例 bean 一样被缓存起来，因此并不会受到容器的管理，也就无需注册销毁方法。
- 对于多例 Bean，Spring 框架并不会自动进行销毁。如果用户不手动销毁多例 Bean，那么这些 Bean 将一直存在于内存中，直到应用程序关闭或者运行时出现内存不足等异常情况。
- 因此，为了避免内存泄漏等问题，建议在使用完多例Bean后手动进行销毁。可以通过在 Bean 定义中指定销毁方法的方式，或者在代码中调用 BeanFactory 的 destroyBean() 方法来实现销毁操作。

```java
private void registerDisposableBeanIfNecessary(Object bean, String beanName, BeanDefinition beanDefinition) {
    // 增加非单例bean不需要执行销毁方法
    if (beanDefinition.isSingleton()) {
        // 接口 或 xml 两种
        if (bean instanceof DisposableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())) {
            registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
        }
    }
}
```

### 4、xml 配置解析中读取 scope 属性并设置到 BeanDefinition 中

- xml 配置解析不在这里展开，详见代码

## 二、scope 简单测试

- 具体代码见 test 包内容

```java
@Test
public void test_applicationContext() {
    // 1. 创建 ApplicationContext （构造器内触发refresh流程）
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

    // 增加：手动调用注册 shutdownHook 到 runtime（这一步应该做成自动的）
    //applicationContext.registerShutdownHook();

    // 2. 获取 bean
    UserService userService = applicationContext.getBean("userService", UserService.class);

    UserService userService1 = applicationContext.getBean("userService", UserService.class);

    System.out.println(userService == userService1); // false;
}
```

## 三、工厂 Bean FactoryBean 实现

- FactoryBean是一种特殊的bean，当向容器获取该bean时，容器不是返回其本身，而是返回其FactoryBean#getObject方法的返回值，可通过编码方式定义复杂的bean。
- 实现逻辑比较简单，当容器发现bean为FactoryBean类型时，调用其getObject方法返回最终bean。当FactoryBean#isSingleton==true，将最终bean放进缓存中，下次从缓存中获取。

### 1、定义 FactoryBean 顶层工厂 Bean 接口

```java
public interface FactoryBean<T> {

    /**
     * 实现了工厂bean接口最终返回的对象
     *
     * @return
     * @throws BeansException
     */
    T getObject() throws Exception;

    /**
     * 是否单例，单例仍然放入单例池进行缓存
     *
     * @return
     */
    boolean isSingleton();
}
```

### 2、AbstractBeanFactory 的 doGetBean 方法增加 FactoryBean 接口的判断

- 创建完成 Bean 实例之后，需要增加一步 FactoryBean 接口判断，getObjectForBeanInstance 方法实现
- 本类增加 factoryBeanObjectCache map 映射，保存 工厂 Bean 的对象，同样需要有单例多例的逻辑

```java
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    // 省略其他无关代码 ....

    // 增加：持有 beanPostProcessors
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    @Override
    public Object getBean(String beanName) throws BeansException {
        return doGetBean(beanName, null);
    }

    @Override
    public Object getBean(String beanName, Object ... args) throws BeansException {
        return doGetBean(beanName, args);
    }

    /**
     * getBean 的模板方法
     *
     * @param beanName
     * @param args
     * @return
     * @param <T>
     */
    protected <T> T doGetBean(String beanName, Object[] args) {
        // 1. 获取单例 Bean
        Object bean = getSingleton(beanName);
        if (bean != null) {
            // 1.1 获取到的bean非空则需要判断是否实现了工厂bean接口（包装一个方法进行实现）
            return getObjectForBeanInstance(bean, beanName);
        }
        // 2. 单例 Bean 不存在则创建 Bean
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        // 3. 根据 beanDefinition 创建 Bean
        bean = createBean(beanName, beanDefinition, args);
        // 3.1 bean 为空且创建完成后，同样需要判断是否实现了工厂bean接口
        return getObjectForBeanInstance(bean, beanName);
    }

    private <T> T getObjectForBeanInstance(Object bean, String beanName) {
        Object obj = bean;
        if (bean instanceof FactoryBean) {
            FactoryBean<?> factoryBean = (FactoryBean<?>) bean;
            // 1. 工厂bean是单例，则从缓存map中取出
            try {
                if (factoryBean.isSingleton()) {
                    obj = this.factoryBeanObjectCache.get(beanName);
                    if (obj == null) {
                        // 2. 缓存中没有，则调用getObject来获取并存到缓存中
                        obj = factoryBean.getObject();
                        this.factoryBeanObjectCache.put(beanName, obj);
                    }
                } else {
                    // 3. 工厂bean是prototype，则无需添加到缓存
                    obj = factoryBean.getObject();
                }
            } catch (Exception e) {
                throw new BeansException("工厂bean抛出异常，e: {}", e);
            }
        }
        return (T) obj;
    }
}
```

## 四、工厂 Bean 简单测试

- 具体代码见 test 包内容

```java
@Test
    public void test_applicationContext() {
        // 1. 创建 ApplicationContext （构造器内触发refresh流程）
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

        Object userService = applicationContext.getBean("userService");

        // userService: class cn.itnxd.springframework.bean.Car
        System.out.println("userService: " + userService.getClass());
    }
```