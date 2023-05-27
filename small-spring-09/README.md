# 第九章 - 容器事件和事件监听器

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
│  │  │              │      │  Aware.java
│  │  │              │      │  BeanFactory.java
│  │  │              │      │  BeanFactoryAware.java
│  │  │              │      │  ConfigurableBeanFactory.java
│  │  │              │      │  ConfigurableListableBeanFactory.java
│  │  │              │      │  DisposableBean.java
│  │  │              │      │  FactoryBean.java
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
│  │  │              │  │  ApplicationContextAware.java
│  │  │              │  │  ApplicationEvent.java
│  │  │              │  │  ApplicationEventPublisher.java
│  │  │              │  │  ApplicationListener.java
│  │  │              │  │  ConfigurableApplicationContext.java
│  │  │              │  │
│  │  │              │  ├─event
│  │  │              │  │      AbstractApplicationEventMulticaster.java
│  │  │              │  │      ApplicationContextEvent.java
│  │  │              │  │      ApplicationEventMulticaster.java
│  │  │              │  │      ContextClosedEvent.java
│  │  │              │  │      ContextRefreshedEvent.java
│  │  │              │  │      SimpleApplicationEventMulticaster.java
│  │  │              │  │
│  │  │              │  └─support
│  │  │              │          AbstractApplicationContext.java
│  │  │              │          AbstractRefreshableApplicationContext.java
│  │  │              │          AbstractXmlApplicationContext.java
│  │  │              │          ApplicationContextAwareProcessor.java
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
│      │              │      Car.java
│      │              │      UserMapper.java
│      │              │      UserService.java
│      │              │
│      │              ├─event
│      │              │      ContextClosedEventListener.java
│      │              │      ContextRefreshedEventListener.java
│      │              │      CustomEvent.java
│      │              │      CustomEventListener.java
│      │              │
│      │              └─processor
│      │                      MyBeanFactoryPostProcessor.java
│      │                      MyBeanPostProcessor.java
│      │
│      └─resources
│              spring.xml
```


## 二、基于观察者模式实现容器事件和事件监听器

### 1、定义顶层事件抽象类

- EventObject 是 Java 事件模型中的一个核心类之一。它定义了事件对象的基本结构和行为，用于在事件源和事件监听器之间传递事件信息。
- 在 Java 的事件模型中，事件源是产生事件的对象，而事件监听器则是用于监听事件的对象。
- 当事件源发生某个事件时，它会创建一个事件对象，并将该事件对象传递给所有注册的事件监听器。事件监听器可以通过该事件对象获取事件的详细信息，并采取相应的处理措施。

- EventObject 持有的是有参构造，没有默认构造，因此继承该类的子类必须手动调用其构造方法进行初始化。
- 这里在子类的构造方法中进行对父类有参构造的显示调用，并传入事件源。

```java
public abstract class ApplicationEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public ApplicationEvent(Object source) {
        super(source);
    }
}
```

### 2、定义几种事件 Event

- 所有的事件都继承自 ApplicationEvent 抽象类
- 传入的事件源都是 ApplicationContext 接口

#### 应用上下文事件 ApplicationContextEvent

- 额外提供一个 getApplicationContext 方法获取上下文对象，调用的是 EventObject 的 getSource 方法

```java
public abstract class ApplicationContextEvent extends ApplicationEvent {

    public ApplicationContextEvent(ApplicationContext source) {
        super(source);
    }

    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}
```

#### 容器刷新事件 ContextRefreshedEvent

```java
public class ContextRefreshedEvent extends ApplicationContextEvent{

    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
}
```

#### 容器关闭事件 ContextClosedEvent

```java
public class ContextClosedEvent extends ApplicationContextEvent{

    public ContextClosedEvent(ApplicationContext source) {
        super(source);
    }
}
```

### 3、定义顶层事件监听器抽象类

- EventListener 是 Java 中用于实现事件监听器的接口,一个实现了 EventListener 接口的类可以注册为事件监听器，并在事件源触发相应事件时接收并处理事件。
- 顶层事件监听器接口定义一个 事件发生的处理 接口，由具体的子类监听器实现处理方法。参数传入需要监听的事件。
- 本监听器接口有一个泛型，泛型是 ApplicationEvent 的子类，会在 onApplicationEvent 方法中作为参数的事件类型

```java
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    /**
     * 定义容器发生事件的监听处理接口
     * @param event 参数是实现了 ApplicationEvent 接口的事件
     */
    void onApplicationEvent(E event);
}
```

### 4、定义注册监听器和发布事件的抽象接口 ApplicationEventMulticaster

- 可以称之为广播器，用来处理一些核心事情：
- 注册监听器：注册传入的监听器
- 移除监听器：移除传入的监听器
- 事件广播接口：**分发**所有支持处理传入事件的**监听器 Listener**来处理这个**事件 Event**

```java
public interface ApplicationEventMulticaster {

    /**
     * 注册监听器
     * @param listener
     */
    void addApplicationListener(ApplicationListener<?> listener);

    /**
     * 移除监听器
     * @param listener
     */
    void removeApplicationListener(ApplicationListener<?> listener);

    /**
     * 事件广播接口
     * @param event
     */
    void multicastEvent(ApplicationEvent event);
}
```

### 5、ApplicationEventMulticaster 的抽象实现类 AbstractApplicationEventMulticaster 

- 实现广播器接口的 add 和 remove 方法
- 额外实现了 BeanFactoryAware 拥有了获取所有 BeanFactory 的能力，存储到本类持有的 BeanFactory 中。
- 本类同时持有 ApplicationListener 监听器集合

```java
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

    private BeanFactory beanFactory;

    // 存储所有的监听器
    public final Set<ApplicationListener<ApplicationEvent>> applicationListeners = new HashSet<>();

    /**
     * 实现父接口的添加监听器方法
     * @param listener
     */
    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.add((ApplicationListener<ApplicationEvent>) listener);
    }

    /**
     * 实现父接口的移除监听器方法
     * @param listener
     */
    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.remove(listener);
    }

    /**
     * 通过aware接口拿到所属的 BeanFactory
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
```

### 6、ApplicationEventMulticaster 的底层实现类 SimpleApplicationEventMulticaster 

- 本类作为底层实现类，实现抽向父类没有实现的 multicastEvent 事件广播接口的逻辑
- 实现事件广播核心方法：即遍历所有的监听器判断是否支持处理传入的事件，支持就处理，不支持就跳过

```java
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster{

    public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
        setBeanFactory(beanFactory);
    }

    /**
     * 事件广播核心方法
     * @param event
     */
    @Override
    public void multicastEvent(ApplicationEvent event) {
        // 遍历所有的监听器
        for (ApplicationListener<ApplicationEvent> applicationListener : applicationListeners) {
            // 监听器支持对本事件处理再进行处理
            if (supportsEvent(applicationListener, event)) {
                applicationListener.onApplicationEvent(event);
            }
        }
    }

    /**
     * 判断监听器是否可以处理指定的事件
     *
     * @param applicationListener
     * @param event
     * @return
     */
    private boolean supportsEvent(ApplicationListener<ApplicationEvent> applicationListener, ApplicationEvent event) {
        // cglib实例化策略getClass获取到是代理类，jdk实例化策略获取到的是本类
        Class<? extends ApplicationListener> listenerClass = applicationListener.getClass();
        Class<?> actualClass = isCglibClass(listenerClass) ? listenerClass.getSuperclass() : listenerClass;

        // 获取 applicationListener 所实现的所有泛型参数的第一个，即这里的事件接口 ApplicationEvent
        Type type = actualClass.getGenericInterfaces()[0];
        // 获取到 ApplicationEvent 传入的具体子类 refresh 或是 close 事件
        Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        String className = actualTypeArgument.getTypeName();
        Class<?> eventClassName = null;
        try {
            eventClassName = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BeansException("事件类型不存在 【{" + className + "}】");
        }
        // 判断event是不是ApplicationEvent子类
        return eventClassName.isAssignableFrom(event.getClass());
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
}
```

### 7、定义顶层事件发布器接口 ApplicationEventPublisher 

- 持有一个发布事件的方法，对传入的事件进行注册发布
- 具体逻辑就是调用广播器的分发接口对事件进行分发处理，支持处理该事件的监听器来对该事件进行处理。

```java
public interface ApplicationEventPublisher {

    void publishEvent(ApplicationEvent event);
}
```

### 8、ApplicationContext 增加 ApplicationEventPublisher 接口实现

```java
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, ResourceLoader, ApplicationEventPublisher {

}
```

### 9、refresh 流程增加三个步骤

1. 初始化事件广播器或分发器 initApplicationEventMulticaster
    - 在 Bean 实例化之前初始化事件广播器
    - 获取到 BeanFactory
    - 创建 SimpleApplicationEventMulticaster 实例保存到本类持有的 SimpleApplicationEventMulticaster 对象
    - 添加到单例池 DefaultSingletonBeanRegistry 注册中心持有的 singletonObjects 进程保存
2. 注册事件监听器 registerListeners
    - 通过 BeanFactory 的 getBean 方法获取到所有的监听器 ApplicationListener
    - 调用广播器或分发器的 addApplicationListener 方法保存到 AbstractApplicationEventMulticaster 持有的 applicationListeners 集合中
3. 发布 refresh 完成事件 finishRefresh
    - refresh 流程最后一步发布**容器刷新完成事件** ContextRefreshedEvent
    - 调用广播器或分发器 applicationEventMulticaster 的 multicastEvent 分发方法对传入的刷新完成事件进行处理，即让所有可以处理本事件的监听器进行处理
4. doClose 方法中增加**容器关闭事件**的发布，即可以通过调用 ApplicationContext.close 方法或在容器销毁时自动调用的 shutdownHook 线程中也会进行本事件的处理。


```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

    // 省略旧代码....

    // ApplicationEventMulticaster 的 beanName
    public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

    private SimpleApplicationEventMulticaster applicationEventMulticaster;

    /**
     * 实现父接口的refresh刷新容器方法
     *
     *
     *
     * refreshBeanFactory、getBeanFactory 由本抽象类的子类进行实现。
     *
     * @throws BeansException
     */
    @Override
    public void refresh() throws BeansException {
        // 1. 刷新BeanFactory：创建BeanFactory，加载BeanDefinition到工厂
        refreshBeanFactory();

        // 2. 获取BeanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();

        // 3. 增加：refresh 流程增加 processor：ApplicationContextAwareProcessor，拥有感知能力
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

        // 4. bean实例化之前执行BeanFactoryPostProcessor
        invokeBeanFactoryPostProcessors(beanFactory);

        // 5. bean初始化之前，注册所有的BeanPostProcessor到容器保存
        registerBeanPostProcessors(beanFactory);

        // 6. 初始化事件发布者
        initApplicationEventMulticaster();

        // 7. 注册事件监听器
        registerListeners();

        // 8. 开始实例化，先实例化单例Bean
        beanFactory.preInstantiateSingletons();

        // 9. 发布事件：容器refresh完成事件
        finishRefresh();
    }

    /**
     * 实例化之前初始化 事件广播器/事件发布者
     */
    private void initApplicationEventMulticaster() {
        // 1. 获取 BeanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        // 2. 创建 ApplicationEventMulticaster
        applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        // 3. 添加到单例池
        beanFactory.addSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
    }

    /**
     * 初始化完发布者注册监听器
     */
    private void registerListeners() {
        // 1. 获取所有监听器
        Collection<ApplicationListener> applicationListeners = getBeansOfType(ApplicationListener.class).values();
        for (ApplicationListener applicationListener : applicationListeners) {
            // 2. 添加到 Set 中进行保存
            applicationEventMulticaster.addApplicationListener(applicationListener);
        }
    }

    /**
     * refresh 完成后，发布容器刷新完成事件
     */
    private void finishRefresh() {
        publishEvent(new ContextRefreshedEvent(this));
    }

    /**
     *
     * @param event
     */
    @Override
    public void publishEvent(ApplicationEvent event) {
        applicationEventMulticaster.multicastEvent(event);
    }

    /**
     * 虚拟机关闭时执行的操作
     */
    public void close() {
        doClose();
    }

    protected void doClose() {

        // 增加发布容器关闭事件
        this.publishEvent(new ContextClosedEvent(this));

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
}

```

## 三、简单测试

- 具体代码见 test 包内容

```java
@Test
public void test_event() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
    // 向容器添加事件
    applicationContext.publishEvent(new CustomEvent(applicationContext, "自定义的发布消息"));
    // 注册销毁方法或者手动调用close方法(doClose方法中会发布ContextClosedEvent事件)
    applicationContext.registerShutdownHook();

    /*
        收到事件【class cn.itnxd.springframework.context.event.ContextRefreshedEvent】消息
        收到事件【class cn.itnxd.springframework.event.CustomEvent】消息：自定义的发布消息
        收到事件【class cn.itnxd.springframework.context.event.ContextClosedEvent】消息
        */
}
```