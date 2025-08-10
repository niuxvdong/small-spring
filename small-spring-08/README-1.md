# 第八章 - 实现 Aware 接口使子类可以感知到容器对象（一）

## 一、项目结构

```java
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
│  │  │              │  │  ConfigurableApplicationContext.java
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
│      │              └─processor
│      │                      MyBeanFactoryPostProcessor.java
│      │                      MyBeanPostProcessor.java
│      │
│      └─resources
│              spring.xml
```

## 二、Aware 感知接口实现

- Aware是感知、意识的意思，Aware接口是标记性接口，其实现子类能感知容器相关的对象。
- 常用的 Aware 接口有 BeanFactoryAware 和 ApplicationContextAware，分别能让其实现者感知**所属**的 BeanFactory 和 ApplicationContext。

### 1、增加顶层标记接口 Aware

- 标记性接口，顶层接口，由各类xxxAware实现，一般用于 instanceof，实现该接口可被容器感知

```java
public interface Aware {
}
```

### 2、增加 BeanFactoryAware 和 ApplicationContextAware

#### BeanFactoryAware

- 实现此接口，能感知到所属的 BeanFactory

```java
public interface BeanFactoryAware extends Aware{

    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
```

#### ApplicationContextAware

- 实现此接口，能感知到所属的 ApplicationContext

```java
public interface ApplicationContextAware extends Aware {

    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
```

### 3、在 initializeBean 方法中实现 BeanFactoryAware 的 set 调用

- 在 initializeBean 方法开始增加 BeanFactory 的设置
- 如果当前 Bean 实现了 BeanFactoryAware，则转换类型去调用对应的 setBeanFactory 方法，将本类 AbstractAutowireCapableBeanFactory 传入充当所属的 BeanFactory


```java
protected Object initializeBean(String beanName, BeanDefinition beanDefinition, Object bean) {
        // 增加：实现BeanFactoryAware接口则向bean设置BeanFactory，即this即可
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }

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
```

### 4、refresh 流程中增加 processor:ApplicationContextAwareProcessor 来实现 ApplicationContextAware 的 set 方法

- 实现 ApplicationContextAware 的接口感知 ApplicationContext，是通过 BeanPostProcessor。
- 由 bean 的生命周期可知，bean 实例化后会经过 BeanPostProcessor 的前置处理和后置处理。
- 定义一个 BeanPostProcessor 的实现类 ApplicationContextAwareProcessor，在 AbstractApplicationContext#refresh 方法中加入到 BeanFactory 中，在前置处理中为 bean 设置所属的 ApplicationContext。

#### 定义 ApplicationContextAwareProcessor

- 持有属性 ApplicationContext，会在 processor 的前置处理中设置后进行保存
- 实现 ApplicationContextAware 接口的类可以通过实现方法 setApplicationContext 获取到所属的 ApplicationContext

```java
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

    private ApplicationContext applicationContext;

    public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 在before方法中进行设置
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
        }
        return bean;
    }

    /**
     * post方法不作操作
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```


#### refresh 流程增加 ApplicationContextAwareProcessor

- ApplicationContextAwareProcessor 保存的 ApplicationContext 是 AbstractApplicationContext 这个对象

```java
@Override
public void refresh() throws BeansException {
    // 1. 刷新BeanFactory：创建BeanFactory，加载BeanDefinition到工厂
    refreshBeanFactory();

    // 2. 获取BeanFactory
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();

    // 增加：refresh 流程增加 processor：ApplicationContextAwareProcessor，拥有感知能力
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

    // 3. bean实例化之前执行BeanFactoryPostProcessor
    invokeBeanFactoryPostProcessors(beanFactory);

    // 4. bean初始化之前，注册所有的BeanPostProcessor到容器保存
    registerBeanPostProcessors(beanFactory);

    // 5. 开始实例化，先实例化单例Bean
    beanFactory.preInstantiateSingletons();
}
```

## 三、Bean 的生命周期图示

- 声明周期中增加了两个 aware 接口
- 可以清晰的看到 BeanFactoryAware 和 ApplicationContextAware 执行的时机

![Bean 的生命周期图示](https://cdn.itnxd.eu.org/gh/niuxvdong/img/pictures/2023/05/25_22_52_16_202305252252286.png)


## 四、简单测试

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

    // 获取aware接口感知到的容器对象
    System.out.println("getApplicationContext: " + userService.getApplicationContext());
    System.out.println("getBeanFactory: " + userService.getBeanFactory());
}
```