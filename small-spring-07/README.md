# 第七章 - 增加初始化方法和销毁方法

## 一、项目结构

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─itnxd
│  │  │          └─springframework
│  │  │              ├─beans
│  │  │              │  │  PropertyValue.java
│  │  │              │  │  PropertyValues.java
│  │  │              │  │
│  │  │              │  ├─exception
│  │  │              │  │      BeansException.java
│  │  │              │  │
│  │  │              │  └─factory
│  │  │              │      │  AutowireCapableBeanFactory.java
│  │  │              │      │  BeanFactory.java
│  │  │              │      │  ConfigurableBeanFactory.java
│  │  │              │      │  ConfigurableListableBeanFactory.java
│  │  │              │      │  DisposableBean.java
│  │  │              │      │  HierarchicalBeanFactory.java
│  │  │              │      │  InitializingBean.java
│  │  │              │      │  ListableBeanFactory.java
│  │  │              │      │
│  │  │              │      ├─config
│  │  │              │      │      BeanDefinition.java
│  │  │              │      │      BeanFactoryPostProcessor.java
│  │  │              │      │      BeanPostProcessor.java
│  │  │              │      │      BeanReference.java
│  │  │              │      │      InstantiationStrategy.java
│  │  │              │      │      SingletonBeanRegistry.java
│  │  │              │      │
│  │  │              │      ├─support
│  │  │              │      │      AbstractAutowireCapableBeanFactory.java
│  │  │              │      │      AbstractBeanDefinitionReader.java
│  │  │              │      │      AbstractBeanFactory.java
│  │  │              │      │      BeanDefinitionReader.java
│  │  │              │      │      BeanDefinitionRegistry.java
│  │  │              │      │      CglibSubclassingInstantiationStrategy.java
│  │  │              │      │      DefaultListableBeanFactory.java
│  │  │              │      │      DefaultSingletonBeanRegistry.java
│  │  │              │      │      DisposableBeanAdapter.java
│  │  │              │      │      SimpleInstantiationStrategy.java
│  │  │              │      │
│  │  │              │      └─xml
│  │  │              │              XmlBeanDefinitionReader.java
│  │  │              │
│  │  │              ├─context
│  │  │              │  │  ApplicationContext.java
│  │  │              │  │  ConfigurableApplicationContext.java
│  │  │              │  │
│  │  │              │  └─support
│  │  │              │          AbstractApplicationContext.java
│  │  │              │          AbstractRefreshableApplicationContext.java
│  │  │              │          AbstractXmlApplicationContext.java
│  │  │              │          ClassPathXmlApplicationContext.java
│  │  │              │
│  │  │              ├─core
│  │  │              │  └─io
│  │  │              │          ClassPathResource.java
│  │  │              │          DefaultResourceLoader.java
│  │  │              │          FileSystemResource.java
│  │  │              │          Resource.java
│  │  │              │          ResourceLoader.java
│  │  │              │          UrlResource.java
│  │  │              │
│  │  │              └─utils
│  │  └─resources
│  │          hello.txt
│  │          spring.xml
│  │
│  └─test
│      ├─java
│      │  └─cn
│      │      └─itnxd
│      │          └─springframework
│      │              │  ApiTest.java
│      │              │
│      │              ├─bean
│      │              │      UserMapper.java
│      │              │      UserService.java
│      │              │
│      │              └─processor
│      │                      MyBeanFactoryPostProcessor.java
│      │                      MyBeanPostProcessor.java
│      │
│      └─resources
│              spring.xml
```

## 二、具体实现步骤

### 1、定义 DisposableBean 和 InitializingBean 接口

- 初始化方法执行时机在创建 bean 完成、属性设置完成之后、执行 BeanPostProcessor 前后置方法的中间执行 invokeInitMethods
- 这个接口可以由用户进行实现，我们的容器在 refresh 的流程中会进行自动扫描注册

```java
public interface InitializingBean {

    /**
     * 在Bean创建完成后，属性注入之后执行初始化方法 invokeInitMethods
     *
     * @throws BeansException
     */
    void afterPropertiesSet() throws BeansException;
}
```

- 销毁方法执行时机在 Bean 销毁，虚拟机关闭之前进行自动调用

```java
public interface DisposableBean {

    /**
     * 在 Bean 销毁，虚拟机关闭之前进行操作
     * @throws BeansException
     */
    void destroy() throws BeansException;
}
```


### 2、修改 BeanDefinition 保存 初始化方法 和 销毁方法

- 初始化方法和销毁方法也是 BeanDefinition 定义信息，因此保存到本类

```java
public class BeanDefinition {

    private Class beanClass;

    // 增加：初始化方法名称
    private String initMethodName;

    /**
     * 增加：销毁方法名称
     *
     * 这两个属性用来保存 spring.xml 配置的 init-method="xxx" destroy-method="xxx" 中的value，以便于反射调用。
     *
     * 还有一种是直接实现 initializingBean 和 disposableBean 接口 注册到容器 中，进行接口方式的调用，
     */
    private String destroyMethodName;

    // 省略其他 ...
}
```

### 3、修改 XmlBeanDefinitionReader 可以解析 init-method 和 destroy-method 属性

- 这个傻瓜式读取详见本类代码，这里不进行展示
- 这个属于配置文件的定义，之前定义的接口属于接口方式的实现，两种方式

### 4、AbstractAutowireCapableBeanFactory 实现 invokeInitMethods 初始化方法执行

- 这里有两种实现：
    - 实现 InitializingBean 接口
    - xml 配置中定义了 init-method 属性

```java
/**
 * bean实例化完成后的初始化流程
 *
 * BeanPostProcessor前置处理，初始化方法执行，BeanPostProcessor后置处理
 *
 * @param beanName
 * @param beanDefinition
 * @param bean
 * @return
 */
protected Object initializeBean(String beanName, BeanDefinition beanDefinition, Object bean) {
    // 1. BeanPostProcessor前置处理
    Object wrapperBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

    // 2. bean 初始化方法执行
    try {
        invokeInitMethods(beanName, wrapperBean, beanDefinition);
    } catch (BeansException e) {
        throw new BeansException("执行 bean 初始化方法失败，e: {}", e);
    }

    // 3. BeanPostProcessor后置处理
    wrapperBean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
    return wrapperBean;
}

/**
 * bean实例化完成后的初始化方法执行
 *
 * 两种初始化实现方式：
 *      1、实现 InitializingBean 接口
 *      2、xml 配置中定义了 init-method 属性
 *
 * @param beanName
 * @param bean
 * @param beanDefinition
 */
protected void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) throws BeansException{
    // 1. 实现了初始化 bean 接口则可以调用 afterPropertiesSet 方法
    if (bean instanceof InitializingBean) {
        ((InitializingBean) bean).afterPropertiesSet();
    }
    // 2. xml 中的 init-method 属性
    String initMethodName = beanDefinition.getInitMethodName();
    if (StrUtil.isNotEmpty(initMethodName)) {
        try {
            // 2.1 反射获取初始化方法
            Method initMethod = beanDefinition.getBeanClass().getMethod(initMethodName);
            // 2.2 反射调用初始化方法
            initMethod.invoke(bean);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new BeansException("找不到xml中定义的 init-method 方法");
        }
    }
}
```

### 5、增加销毁方法的适配器 DisposableBeanAdapter 

- 同样针对销毁方法这里也有两种实现：
    - 实现 DisposableBean 接口
    - xml 配置中定义了 destroy-method 属性
- 这里为何要使用适配器进行适配？
    - 与 initializingBean 接口不同的是，初始化方法可以有多个，因此在执行初始化方法时直接依次执行所有 init 方法即可，包括 xml 中的以及 实现了接口方式的。
    - 而 销毁方法则不同，销毁只需要执行一次即可，因此，需要做一下特殊处理，由于我们并不需要关注要执行哪些销毁方法，因此，可以定义个适配器 adapter 来适配两种类型的销毁方法。
- 销毁的优先级：
    - 实现了 DisposableBean 接口的优先
    - 没有实现接口 或者 实现了接口但是 销毁方法不是 destroy 时，执行配置方式（简而言之：由于 DisposableBean 接口默认销毁方法名是 destroy，这里这个条件也就是说与接口无关的就执行配置文件定义的销毁方法。（**就是要保证接口的默认 destroy 方法只被执行一次**））


```java
public class DisposableBeanAdapter implements DisposableBean {

    private final Object bean;
    private final String beanName;
    private final String destroyMethodName;

    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
        this.bean = bean;
        this.beanName = beanName;
        this.destroyMethodName = beanDefinition.getDestroyMethodName();
    }

    /**
     * 适配器重写方法实现两种无需关注细节的适配
     *
     * @throws BeansException
     */
    @Override
    public void destroy() throws BeansException {
        // 1. 实现接口 DisposableBean 的优先
        if (bean instanceof DisposableBean) {
            ((DisposableBean) bean).destroy();
        }
        // 2. xml 配置的 destroy-method 方法
        // （没有实现接口时执行这里 或者 实现了接口但是 销毁方法不是 destroy 时执行）
        // 这里其实保证了接口中定义的 destroy 方法只执行一次
        if (StrUtil.isNotEmpty(destroyMethodName) && !(bean instanceof DisposableBean && "destroy".equals(destroyMethodName))) {
            try {
                Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
                destroyMethod.invoke(bean);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new BeansException("找不到xml中定义的 destroy-method 方法");
            }
        }
    }
}
```

### 6、注册 DisposableBean 到 DefaultSingletonBeanRegistry

- 由于 DisposableBean 是在最后销毁时才进行调用，因此需要先保存起来
- 持有 disposableBeans 以便于在容器销毁时候进行调用
- 增加一个 destroySingletons 方法，在容器销毁时候进行调用

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    // 存放单例对象
    private Map<String, Object> singletonObjects = new HashMap<>();

    // 增加：存放 disposableBean
    private Map<String, DisposableBean> disposableBeans = new HashMap<>();

    // 省略无关代码 ...

    /**
     * 注册 disposableBean 到单例bean注册中心保存
     * @param beanName
     * @param disposableBean
     */
    public void registerDisposableBean(String beanName, DisposableBean disposableBean) {
        disposableBeans.put(beanName, disposableBean);
    }

    /**
     * 通过接口 disposableBean 方式销毁单例 bean 的方法
     */
    public void destroySingletons() {
        List<String> beanNames = new ArrayList<>(disposableBeans.keySet());
        for (String beanName : beanNames) {
            DisposableBean disposableBean = disposableBeans.remove(beanName);
            try {
                disposableBean.destroy();
            } catch (BeansException e) {
                throw new BeansException("执行销毁 bean 方法时抛出异常 e: {}", e);
            }
        }
    }
}

```

### 7、AbstractAutowireCapableBeanFactory 的 createBean 增加注册销毁接口

- BeanPostProcessor 前后置方法执行完成，包括中间的 初始化方法执行完成后，注册销毁方法
- 销毁方法注册就是调用了DefaultSingletonBeanRegistry 的 registerDisposableBean 方法
- 可以看到注册销毁 Bean 时传入的 DisposableBean 是一个 DisposableBeanAdapter，即这里使用适配器的好处，可以无需关注细节，有适配器提供的销毁方法进行调用。（由适配器写的逻辑来保证销毁方法只会被执行一次）


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

    // 5. 添加到单例缓存 map
    addSingleton(beanName, bean);
    return bean;
}

 /**
 * 初始化完成注册实现了销毁接口的对象
 *
 * @param bean
 * @param beanName
 * @param beanDefinition
 */
private void registerDisposableBeanIfNecessary(Object bean, String beanName, BeanDefinition beanDefinition) {
    // 接口 或 xml 两种
    if (bean instanceof DisposableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())) {
        registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
    }
}
```

### 8、增加虚拟机关闭时的手动调用 close 以及自动调用 registerShutdownHook 来实现容器关闭时的收尾工作

#### ConfigurableBeanFactory 增加 destroySingletons 接口

- 这个接口是 DefaultSingletonBeanRegistry 实现的，二者并没有直接继承关系
- 真正被调用是在 AbstractApplicationContext 的 doClose 方法中

```java
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    /**
     * 拥有添加BeanPostProcessor的方法
     *
     * @param beanPostProcessor
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    /**
     * 添加：销毁单例bean接口
     */
    void destroySingletons();
}
```

#### ConfigurableApplicationContext 定义 close 和 registerShutdownHook

- close：这个是一种手动的调用进行销毁
- registerShutdownHook：是向虚拟机注册 回调函数，自动方式

```java
public interface ConfigurableApplicationContext extends ApplicationContext{

    /**
     * 刷新容器接口，定义整个容器的执行流程。
     *
     * @throws BeansException
     */
    void refresh() throws BeansException;

    /**
     * 也是虚拟机关闭时候调用，为我们手动调用的方式（手动）
     */
    void close();

    /**
     * 向虚拟机注册钩子方法，在虚拟机关闭时候调用（自动）
     */
    void registerShutdownHook();
}
```

#### AbstractApplicationContext 实现手动和自动两种方式的销毁

```java
/**
 * 虚拟机关闭时执行的操作
 */
public void close() {
    doClose();
}

protected void doClose() {
    destroyBeans();
}

/**
 * 真正执行销毁方法的地方
 */
protected void destroyBeans() {
    getBeanFactory().destroySingletons();
}

/**
 * 注册 shutdownHook 在虚拟机关闭时候调用 disposableBean.destroy() 方法
 */
public void registerShutdownHook() {
    // 创建一个线程注入doClose方法在虚拟机关闭时候调用
    Thread shutdownHook = new Thread(this::doClose);
    // 虚拟机关闭时候调用
    Runtime.getRuntime().addShutdownHook(shutdownHook);
}
```

## 三、bean 生命周期图示

![增加了初始化和销毁方法的 bean 生命周期图示](https://gitcode.net/qq_43590403/img/-/raw/master/pictures/2023/05/25_22_27_24_202305252227390.png)
https://gitcode.net/qq_43590403/img/-/raw/master/pictures/2023/05/25_22_27_24_202305252227390.png

## 三、简单测试

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

    userService.getUserInfo();
    System.out.println(userService);

    // 或者：手动调用 close 方法
    applicationContext.close();
}
```