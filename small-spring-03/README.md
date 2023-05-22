# 第三章 - 实现 Bean 的两种实例化策略（JDK & Cglib）

## 一、项目结构

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─itnxd
│  │  │          └─springframework
│  │  │              └─beans
│  │  │                  ├─exception
│  │  │                  │      BeansException.java
│  │  │                  │
│  │  │                  └─factory
│  │  │                      │  BeanFactory.java
│  │  │                      │
│  │  │                      ├─config
│  │  │                      │      BeanDefinition.java
│  │  │                      │      InstantiationStrategy.java
│  │  │                      │      SingletonBeanRegistry.java
│  │  │                      │
│  │  │                      └─support
│  │  │                              AbstractAutowireCapableBeanFactory.java
│  │  │                              AbstractBeanFactory.java
│  │  │                              BeanDefinitionRegistry.java
│  │  │                              CglibSubclassingInstantiationStrategy.java
│  │  │                              DefaultListableBeanFactory.java
│  │  │                              DefaultSingletonBeanRegistry.java
│  │  │                              SimpleInstantiationStrategy.java
│  │  │
```

## 二、增加实例化策略

### 1、回顾之前如何实例化

```java
bean = beanDefinition.getBeanClass().newInstance();
```

- 很明显，如何离谱的实例化只能处理无参构造，接下来我们添加两种实例化策略来支持复杂的有参构造的实例化。

### 2、增加顶层实例化接口

- 需要提供 BeanDefinition 信息、beanName、以及构造器 ctor、构造器参数值 args 来进行实例化。

```java
public interface InstantiationStrategy {

    /**
     * 实例化接口
     *
     * @param beanDefinition BeanDefinition 信息
     * @param beanName BeanName
     * @param ctor 构造器
     * @param args 构造器参数
     * @return 返回实例化对象
     * @throws BeansException
     */
    Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException;
}
```

### 3、添加 JDK 实例化策略

- 很简单，利用反射根据给定的构造器和构造器对应的参数进行实例化即可。

```java
public class SimpleInstantiationStrategy implements InstantiationStrategy {

    /**
     *
     * @param beanDefinition BeanDefinition 信息
     * @param beanName BeanName
     * @param ctor 构造器（会包含构造器的参数类型和数量）
     * @param args 构造器参数（匹配ctor的参数value）
     * @return
     * @throws BeansException
     */
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            if (ctor != null) {
                return beanClass.getDeclaredConstructor(ctor.getParameterTypes()).newInstance(args);
            } else {
                return beanClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new BeansException("jdk实例化【" + beanClass.getName() + "】失败。", e);
        }
    }
}
```

### 4、添加 Cglib 实例化策略

#### 添加 Cglib 依赖支持

```xml
<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.3.0</version>
</dependency>
```

#### 实现实例化策略

```java
public class CglibSubclassingInstantiationStrategy implements InstantiationStrategy {

    /**
     * 引入Cglib依赖实现（根据父类实现子类）
     *
     * CGLIB是一个基于ASM的字节码处理框架，可以通过动态生成子类来实现AOP等功能
     *
     * 使用CGLIB创建代理类时，被代理类必须有默认构造函数。如果被代理类没有默认构造函数，
     * 可以使用Enhancer的setCallbackType方法设置回调函数接口，然后在回调函数实现类中调用有参构造函数来创建对象。
     *
     * @param beanDefinition BeanDefinition 信息
     * @param beanName BeanName
     * @param ctor 构造器
     * @param args 构造器参数
     * @return
     * @throws BeansException
     */
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        // 1. 创建Enhancer对象，用于生成代理类
        Enhancer enhancer = new Enhancer();
        // 2. 设置代理类的父类或接口
        enhancer.setSuperclass(beanDefinition.getBeanClass());
        // 3. 设置回调函数，即拦截方法的逻辑处理
        /*
          CGLIB的NoOp接口是一个空的方法拦截器，它可以在创建代理类时作为回调函数，用于对目标类的方法进行空处理，即不做任何操作，直接返回原方法的返回值。
          使用NoOp接口作为回调函数，可以在某些情况下提高代理类的性能，避免不必要的方法拦截和处理。

          注意：NoOp.INSTANCE是NoOp接口的一个实例，表示空的方法拦截器。
          使用NoOp接口作为回调函数时，目标类的方法必须是非final的，否则代理类无法覆盖该方法。
         */
        enhancer.setCallback(NoOp.INSTANCE);
        // 4. 生成代理类实例
        if (ctor == null) return enhancer.create();
        return enhancer.create(ctor.getParameterTypes(), args);
    }
}
```

### 5、修改 AbstractAutowireCapableBeanFactory 的 createBean 方法实现

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{

    // 添加：持有实例化策略来根据策略实例化对象(默认为cglib策略)
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

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
            // 1. 根据 BeanDefinition 创建 Bean
            //bean = beanDefinition.getBeanClass().newInstance();
            bean = createBeanInstance(beanName, beanDefinition, args);
        } catch (BeansException e) {
            throw new BeansException("初始化Bean失败: ", e);
        }

        // 2. 添加到单例缓存 map
        addSingleton(beanName, bean);
        return bean;
    }

    /**
     * 抽取创建Bean的逻辑，调用本类持有的实例化策略进行实例化
     *
     * @param beanName
     * @param beanDefinition
     * @param args
     * @return
     */
    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException{

        // 1. 获取所有构造器
        Constructor[] declaredConstructors = beanDefinition.getBeanClass().getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            // 2. 简单比较参数数量即可（忽略类型比较）
            if(args != null && args.length == constructor.getParameterCount()) {
                // 3. 根据实例化策略创建对象
                return getInstantiationStrategy().instantiate(beanDefinition, beanName, constructor, args);
            }
        }
        // 4. 无匹配构造器抛出异常
        throw new BeansException("没有与当前参数匹配的构造器进行实例化，实例化失败。");
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
```

## 三、简单测试

```java
public class ApiTest {

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 注入 BeanDefinition
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class);
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        // 3. 获取 Bean
        UserService userService = (UserService) beanFactory.getBean("userService", "往事如烟");
        userService.getUserInfo();
    }
}
```