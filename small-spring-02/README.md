# 第二章 - 实现 Bean 的定义、注册、获取

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
│  │  │                      │      SingletonBeanRegistry.java
│  │  │                      │
│  │  │                      └─support
│  │  │                              AbstractAutowireCapableBeanFactory.java
│  │  │                              AbstractBeanFactory.java
│  │  │                              BeanDefinitionRegistry.java
│  │  │                              DefaultListableBeanFactory.java
│  │  │                              DefaultSingletonBeanRegistry.java
│  │  │
│  │  └─resources
│  └─test
│      └─java
│          └─cn
│              └─itnxd
│                  └─springframework
│                      │  ApiTest.java
│                      │
│                      └─bean
│                              UserService.java

```

## 二、实现过程

### 1、修改 BeanDefinition 定义信息

BeanDefinition 不保存实例化好的 Bean 对象，只保存 Bean 的定义信息。
修改保存的 Bean 对象为 Bean 的具体类型。

```java
private Class beanClass;
```

### 2、添加 SingletonBeanRegistry 单例 Bean 注册中心顶层接口

本类只有一个接口，获取单例 Bean 对象。

```java
public interface SingletonBeanRegistry {

    /**
     * 本接口只有一个方法获取单例Bean
     * @param beanName
     * @return
     */
    Object getSingleton(String beanName);
}
```

### 3、添加 BeanFactory 顶层 Bean 工厂接口

顶层 Bean 工厂只持有一个方法，getBean 方法

```java
public interface BeanFactory {

    /**
     * 唯一的一个方法，获取 Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName) throws BeansException;
}
```

### 4、添加 SingletonBeanRegistry 实现类 DefaultSingletonBeanRegistry

- 主要实现单例注册中心接口 getSingleton 方法
- 为了保存单例对象，本类会持有一个 map （singletonObjects）来保存单例对象，当然要支持 get 和 put

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    // 存放单例对象
    private Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * 实现顶层单例接口的唯一个获取单例对象的方法
     * @param beanName
     * @return
     */
    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    /**
     * 一个 protected 方法，保存单例对象到 map 中
     * 可以被继承本类的子类调用，主要包括：AbstractBeanFactory 以及继承的 DefaultListableBeanFactory 调用
     * @param beanName
     * @param singletonObject
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }
}
```

### 5、添加 BeanFactory 的抽象实现类 AbstractBeanFactory

- 主要实现 BeanFactory 顶层接口的 getBean 方法
- 继承 DefaultSingletonBeanRegistry 获得了单例 Bean 的 get 和 put 能力
- 由于是抽象类，可以利用**模板方法模式**，将 createBean 和 getBeanDefinition 方法定义为抽象方法由具体子类进行实现
- **AbstractAutowireCapableBeanFactory**、**DefaultListableBeanFactory**，这两个类继承本类分别做了相应的实现处理

```java
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {

    /**
     * 1. 实现顶层 BeanFactory 接口的唯一方法 <br>
     * 2. 这也是本抽象类 AbstractBeanFactory 的模板方法模式的体现，本方法即为模板方法，定义了整个骨架 <br>
     *
     * 3. 模板方法设计模式笔记：<a href="https://blog.itnxd.cn/article/behavioral-pattern-template-design-pattern">https://blog.itnxd.cn/article/behavioral-pattern-template-design-pattern</a>
     *
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName) throws BeansException {
        // 1. 获取单例 Bean
        Object bean = getSingleton(beanName);
        if (bean != null) {
            return bean;
        }
        // 2. 单例 Bean 不存在则创建 Bean
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        // 3. 根据 beanDefinition 创建 Bean
        return createBean(beanName, beanDefinition);
    }

    /**
     * 本抽象类只单纯实现getBean
     * 创建 Bean 由子类实现
     * @param beanName
     * @param beanDefinition
     * @return
     */
    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException;

    /**
     * 根据 beanName 获取 BeanDefinition 信息
     * @param beanName
     * @return
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;
}
```

### 6、AbstractAutowireCapableBeanFactory 继承 AbstractBeanFactory 实现核心 createBean 方法

- 继承 AbstractBeanFactory 实现核心 createBean 方法
- 本身也是抽象类，由继承本类的子类 DefaultListableBeanFactory 真正使用

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{

    /**
     * 实现父抽奖类 AbstractBeanFactory 其中一个抽象方法 createBean
     * @param beanName
     * @param beanDefinition
     * @return
     * @throws BeansException
     */
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        Object bean = null;
        try {
            // 1. 根据 BeanDefinition 创建 Bean
            // TODO 这里newInstance无法处理有参构造
            bean = beanDefinition.getBeanClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeansException("初始化Bean失败", e);
        }

        // 2. 添加到单例缓存 map
        addSingleton(beanName, bean);
        return bean;
    }
}
```

### 7、添加 BeanDefinitionRegistry 定义信息注册接口

- 提供向 beanDefinitionMap 注册的能力
- 由实现类 DefaultListableBeanFactory 实现 registerBeanDefinition 方法

```java
public interface BeanDefinitionRegistry {

    /**
     * 只定义 BeanDefinition 注册接口
     * @param beanName
     * @param beanDefinition
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
}
```


### 8、BeanFactory 核心实现类 DefaultListableBeanFactory，最底层实现类

- **持有**存放 beanName 和 BeanDefinition 信息映射的 map，**beanDefinitionMap**
- 继承 AbstractAutowireCapableBeanFactory 抽象类，有了 createBean 方法实现
- 实现 BeanDefinitionRegistry，实现 registerBeanDefinition 注册定义方法
- 实现 AbstractBeanFactory 抽象类的 getBeanDefinition 方法
- 自此，一个基本功能的 BeanFactory 就此实现！

```java
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry{

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 实现抽象类 AbstractBeanFactory 定义的抽象方法获取 BeanDefinition
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new BeansException(beanName + "没有被定义过");
        }
        return beanDefinition;
    }

    /**
     * 实现 BeanDefinitionRegistry 的接口，注册 BeanDefinition
     * @param beanName
     * @param beanDefinition
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }
}
```

## 三、类结构图

- BeanDefinitionRegistry 提供注册 BeanDefinition 信息到 DefaultListableBeanFactory 的 beanDefinitionMap 的接口定义
- SingletonBeanRegistry 提供获取单例 Bean 的接口定义
- DefaultSingletonBeanRegistry 持有 singletonObjects，实现 SingletonBeanRegistry 的获取单例 bean 的接口
- BeanFactory 为顶层 BeanFactory，定义 getBean 的核心方法
- AbstractBeanFactory 抽象类继承 DefaultSingletonBeanRegistry 实现 BeanFactory，拥有二者能力
- AbstractAutowireCapableBeanFactory 继承 AbstractBeanFactory 实现 createBean 核心方法
- DefaultListableBeanFactory 为最底层实现类，拥有上面的所有能力；同时持有 beanDefinitionMap Bean 定义信息的保存能力 

![https://cdn.itnxd.eu.org/gh/niuxvdong/img/pictures/2023/05/13_18_40_35_202305131840113.png](https://cdn.itnxd.eu.org/gh/niuxvdong/img/pictures/2023/05/13_18_40_35_202305131840113.png)


## 四、简单测试

```java
public class ApiTest {

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 注入 BeanDefinition
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class);
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        // 3. 第一次获取 Bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();

        // 4. 第二次获取 Bean from singleton
        UserService userServiceSingle = (UserService) beanFactory.getBean("userService");
        userServiceSingle.getUserInfo();

        System.out.println(userService == userServiceSingle); // true
    }
}
```


