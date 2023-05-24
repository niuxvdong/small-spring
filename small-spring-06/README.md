# 第六章 - 应用上下文 ApplicationContext 实现 

## 一、项目结构

```
│
└─src
    ├─main
    │  ├─java
    │  │  └─cn
    │  │      └─itnxd
    │  │          └─springframework
    │  │              ├─beans
    │  │              │  │  PropertyValue.java
    │  │              │  │  PropertyValues.java
    │  │              │  │
    │  │              │  ├─exception
    │  │              │  │      BeansException.java
    │  │              │  │
    │  │              │  └─factory
    │  │              │      │  AutowireCapableBeanFactory.java
    │  │              │      │  BeanFactory.java
    │  │              │      │  ConfigurableBeanFactory.java
    │  │              │      │  ConfigurableListableBeanFactory.java
    │  │              │      │  HierarchicalBeanFactory.java
    │  │              │      │  ListableBeanFactory.java
    │  │              │      │
    │  │              │      ├─config
    │  │              │      │      BeanDefinition.java
    │  │              │      │      BeanFactoryPostProcessor.java
    │  │              │      │      BeanPostProcessor.java
    │  │              │      │      BeanReference.java
    │  │              │      │      InstantiationStrategy.java
    │  │              │      │      SingletonBeanRegistry.java
    │  │              │      │
    │  │              │      ├─support
    │  │              │      │      AbstractAutowireCapableBeanFactory.java
    │  │              │      │      AbstractBeanDefinitionReader.java
    │  │              │      │      AbstractBeanFactory.java
    │  │              │      │      BeanDefinitionReader.java
    │  │              │      │      BeanDefinitionRegistry.java
    │  │              │      │      CglibSubclassingInstantiationStrategy.java
    │  │              │      │      DefaultListableBeanFactory.java
    │  │              │      │      DefaultSingletonBeanRegistry.java
    │  │              │      │      SimpleInstantiationStrategy.java
    │  │              │      │
    │  │              │      └─xml
    │  │              │              XmlBeanDefinitionReader.java
    │  │              │
    │  │              ├─context
    │  │              │  │  ApplicationContext.java
    │  │              │  │  ConfigurableApplicationContext.java
    │  │              │  │
    │  │              │  └─support
    │  │              │          AbstractApplicationContext.java
    │  │              │          AbstractRefreshableApplicationContext.java
    │  │              │          AbstractXmlApplicationContext.java
    │  │              │          ClassPathXmlApplicationContext.java
    │  │              │
    │  │              └─core
    │  │                  └─io
    │  │                          ClassPathResource.java
    │  │                          DefaultResourceLoader.java
    │  │                          FileSystemResource.java
    │  │                          Resource.java
    │  │                          ResourceLoader.java
    │  │                          UrlResource.java
    │  │
    │  └─resources
    │          hello.txt
    │          spring.xml
    │
    └─test
        ├─java
        │  └─cn
        │      └─itnxd
        │          └─springframework
        │              │  ApiTest.java
        │              │
        │              ├─bean
        │              │      UserMapper.java
        │              │      UserService.java
        │              │
        │              └─processor
        │                      MyBeanFactoryPostProcessor.java
        │                      MyBeanPostProcessor.java
        │
        └─resources
                spring.xml

```

## 二、ApplicationContext 应用上下文实现

### 1、添加各种 BeanFactory 的子接口定义

#### 修改 BeanFactory 添加按类型获取 Bean 的接口

```java
public interface BeanFactory {

    // 省略旧代码 ....

    /**
     * 增加根据bean名称和类型获取bean的方法
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    <T> T getBean(String beanName, Class<T> type) throws BeansException;
}
```

#### 添加 HierarchicalBeanFactory 子接口

- 本接口是一个继承了 BeanFactory 顶层接口的空实现，是一个标识性接口，表示实现本接口的类可以获取到父类 BeanFactory

```java
public interface HierarchicalBeanFactory extends BeanFactory{

}
```

#### 添加 ListableBeanFactory 子接口

- 本接口添加了根据类型获取 Bean 的方法，返回 beanName 与 Bean 对象映射的 map 集合
- 还定义了一个获取容器中注册的所有 BeanDefinition 信息的方法

```java
public interface ListableBeanFactory extends BeanFactory{

    /**
     * 拓展获取bean的方法，根据类型获取bean，返回一个指定类型的所有实例bean
     *
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

    /**
     * 返回容器中注册过的所有BeanDefinition信息
     *
     * @return
     */
    String[] getBeanDefinitionNames();
}
```

#### 添加 AutowireCapableBeanFactory 子接口

- 本子接口拥有两个方法，即处理 BeanPostProcessors 的前后置方法
- BeanPostProcessors 的方法会在 bean 对象被实例化之后（**createBeanInstance**）、设置完属性之后（**applyPropertyValues**），进行 BeanPostProcessors 的前后置处理（**initializeBean**）

```java
public interface AutowireCapableBeanFactory extends BeanFactory{

    /**
     * 用来执行BeanPostProcessors的beanPostProcessorsBeforeInitialization方法
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) throws BeansException;

    /**
     * 用来执行BeanPostProcessors的beanPostProcessorsAfterInitialization方法
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) throws BeansException;
}
```

#### 添加 ConfigurableBeanFactory 子接口

- 继承 HierarchicalBeanFactory 子接口拥有了可以获取父类 BeanFactory 的能力
- 继承 SingletonBeanRegistry 拥有了获取单例bean的能力

```java
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    /**
     * 拥有添加BeanPostProcessor的方法
     *
     * @param beanPostProcessor
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
```

#### 添加 ConfigurableListableBeanFactory 子接口

- 继承 ListableBeanFactory 拥有了根据类型获取 Bean 的能力
- 继承 AutowireCapableBeanFactory 拥有了处理属性设置之后 beanPostProcessor 的前后置处理
- 继承 ConfigurableBeanFactory 拥有了添加 BeanPostProcessor 的能力
- 本子接口定义了两个方法，一个是根据 beanName 获取 BeanDefinition 信息的方法，一个是实例化单例 Bean 的接口（本接口会由**核心底层类 DefaultListableBeanFactory** 进行实现）

```java
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

    /**
     * 根据beanName获取BeanDefinition的方法
     *
     * @param beanName
     * @return
     */
    BeanDefinition getBeanDefinitionName(String beanName);

    /**
     * 提前实例化所有单例实例的方法
     *
     * @throws BeansException
     */
    void preInstantiateSingletons() throws BeansException;
}
```

### 2、增加 BeanFactoryPostProcessor 和 BeanPostProcessor 定义

#### BeanFactoryPostProcessor

- BeanFactoryPostProcessor 的执行时间是：**会在 BeanDefinition 注册完成后，Bean实例化之前**，提供**修改 BeanDefinition 属性**的机制
- 具体执行流程详见下方关于 **ApplicationContext 的 refresh 流程**

```java
public interface BeanFactoryPostProcessor {

    /**
     * 在 BeanDefinition 注册完成后，Bean实例化之前，提供修改BeanDefinition属性的机制
     *
     * @param beanFactory
     * @throws BeansException
     */
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
```

#### BeanPostProcessor

- BeanPostProcessor 的执行时间是：在 bean 对象被实例化之后（**createBeanInstance**）、设置完属性之后（**applyPropertyValues**），进行 BeanPostProcessors 的前后置处理（**initializeBean**）
- 提供**修改 Bean** 的机制

```java
public interface BeanPostProcessor {

    /**
     * 见名知意，即在 Bean 实例化完成之后执行初始化方法之前（属性填充，空bean）进行修改Bean实例的机制
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

    /**
     * 见名知意，即在 Bean 实例化完成之后执行初始化方法之后（属性填充，非空Bean）进行修改Bean实例的机制
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
}
```

### 3、添加 ApplicationContext 定义及抽象类和子接口

#### ApplicationContext 顶层上下文接口定义

> ApplicationContext 除了拥有 BeanFactory 的所有功能外，还支持特殊类型 bean，如：BeanFactoryPostProcessor 和BeanPostProcessor的自动识别、资源加载、容器事件和监听器、国际化支持、单例 bean 自动初始化等。BeanFactory 是 spring 的基础设施，面向 spring 本身；而 ApplicationContext 面向 spring 的使用者。（简而言之：ApplicationContext 定义了一个 refresh 流程进行初始化操作）

- 本顶层接口没有任何具体逻辑编写，逻辑都由抽象类定义，子接口或子类进行实现
- 继承 HierarchicalBeanFactory 拥有了获取父类 BeanFactory 的能力
- 继承 ListableBeanFactory 拥有了各种获取 bean 的方法，包括根据类型获取
- 继承 ResourceLoader 拥有了获取资源加载器的能力，以便于得到资源 Resource，进而得到 InputStream 输入流进行解析注册 BeanDefinition 信息到 DefaultListableBeanFactory 的 beanDefinitionMap 中

```java
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, ResourceLoader {
}
```

#### ConfigurableApplicationContext 子接口定义核心的 refresh 方法

- 本类只进行定义，不进行实现，具体实现由抽象类去定义具体的 refresh 流程

```java
public interface ConfigurableApplicationContext extends ApplicationContext{

    /**
     * 刷新容器接口，定义整个容器的执行流程。
     *
     * @throws BeansException
     */
    void refresh() throws BeansException;
}
```

#### AbstractApplicationContext 抽象类来实现子接口定义的核心 refresh 方法

- 实现子接口 ConfigurableApplicationContext 的 refresh 方法
- 继承 DefaultResourceLoader 即拥有了了顶层 ApplicationContext 的接口 ResourceLoader 的具体实现
- refresh 流程目前只定义了五个步骤，本类只实现第三步和第四步，其他步骤由本类的子类进行实现
    1. 刷新 BeanFactory：创建BeanFactory，加载BeanDefinition到工厂（由子类实现）
    2. 获取 BeanFactory（由子类实现）
    3. bean 实例化之前执行 BeanFactoryPostProcessor：提供对 BeanFactory 的拓展修改能力
    4. bean 初始化之前，注册所有的 BeanPostProcessor 到容器保存：保存起来以便于整整初始化时候进行使用，提供对 bean 实例的拓展修改能力
    5. 开始实例化，先实例化单例 Bean。（很明显，这里会进入核心的 getBean 流程，进行进入整个创建 bean 实例以及 BeanPostProcessor 前后置的处理）（由子类实现）
- 这里会发现一点，本类实现了许多 getBean 方法，而他的实现逻辑是直接调用的 BeanFactory 的具体实现类的 getBean 方法，即 ApplicationContext 是拥有了所有 BeanFactory 的功能（拿来主义）。而这里的 BeanFactory 观察实现会发现用的就是最底层的 BeanFactory 的实现类 **DefaultListableBeanFactory**

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

    /**
     * 实现父接口的refresh刷新容器方法
     *
     * 本类只实现第三步和第四步！
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

        // 3. bean实例化之前执行BeanFactoryPostProcessor
        invokeBeanFactoryPostProcessors(beanFactory);

        // 4. bean初始化之前，注册所有的BeanPostProcessor到容器保存
        registerBeanPostProcessors(beanFactory);

        // 5. 开始实例化，先实例化单例Bean
        beanFactory.preInstantiateSingletons();
    }

    /**
     * 刷新BeanFactory：创建BeanFactory，加载BeanDefinition到工厂
     *
     * @throws BeansException
     */
    protected abstract void refreshBeanFactory() throws BeansException;

    /**
     * 获取BeanFactory
     *
     * @return
     */
    protected abstract ConfigurableListableBeanFactory getBeanFactory();

    /**
     * bean实例化之前执行BeanFactoryPostProcessor
     *
     * @param beanFactory
     */
    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 1. 根据类型获取到容器中的所有BeanFactoryPostProcessor
        Map<String, BeanFactoryPostProcessor> beanFactoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);
        for (BeanFactoryPostProcessor beanFactoryPostProcessor : beanFactoryPostProcessorMap.values()) {
            // 2. 调用BeanFactoryPostProcessor的方法去执行processor
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    /**
     * bean初始化之前，注册所有的BeanPostProcessor到容器保存
     *
     * @param beanFactory
     */
    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 1. 获取到所有的BeanPostProcessor
        Map<String, BeanPostProcessor> beanPostProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorMap.values()) {
            // 2. 向BeanFactory中注册BeanPostProcessor保存
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
    }

    /**
     * 实现顶层BeanFactory的根据类型获取bean实例的方法
     *
     * @param beanName
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> T getBean(String beanName, Class<T> type) throws BeansException {
        return getBeanFactory().getBean(beanName, type);
    }

    /**
     * 实现父接口ListableBeanFactory的根据类型获取bean的方法
     *
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 重新实现顶层BeanFactory的根据beanName获取bean的方法，可以看到其实还是调用的AbstractBeanFactory的实现
     *
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName) throws BeansException {
        return getBeanFactory().getBean(beanName);
    }

    /**
     * 补充顶层BeanFactory的getBean方法实现
     *
     * @param beanName
     * @param args
     * @return
     * @throws BeansException
     */
    @Override
    public Object getBean(String beanName, Object... args) throws BeansException {
        return getBeanFactory().getBean(beanName, args);
    }

    /**
     * 实现父接口ListableBeanFactory获取所有BeanDefinitionName的方法
     *
     * @return
     */
    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }
}
```

### 4、AbstractBeanFactory 增加 ConfigurableBeanFactory 实现 addBeanPostProcessor 方法

- 修改 getBean 方法为调用 doGetBean
- 实现顶层 BeanFactory 新添加的 getBean 方法 
- 本类增加一个 List 集合来保存 BeanPostProcessor，实现 ConfigurableBeanFactory 的 addBeanPostProcessor 方法，包括增加一个 get 方法

```java
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    // 增加：持有 beanPostProcessors
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

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
        return doGetBean(beanName, null);
    }

    /**
     * 增加：添加支持入参的 getBean 方法
     * @param beanName
     * @param args
     * @return
     * @throws BeansException
     */
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
            return (T) bean;
        }
        // 2. 单例 Bean 不存在则创建 Bean
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        // 3. 根据 beanDefinition 创建 Bean
        return (T) createBean(beanName, beanDefinition, args);
    }

    /**
     * 实现新增的方法
     *
     * @param beanName
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> T getBean(String beanName, Class<T> type) throws BeansException {
        return (T) getBean(beanName);
    }

    /**
     * 本抽象类只单纯实现getBean
     * 创建 Bean 由子类实现
     *
     * 修改：增加入参
     *
     * @param beanName
     * @param beanDefinition
     * @param args
     * @return
     */
    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException;

    /**
     * 根据 beanName 获取 BeanDefinition 信息
     * @param beanName
     * @return
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * 实现ConfigurableBeanFactory的添加BeanPostProcessor方法
     *
     * @param beanPostProcessor
     */
    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        // 有则覆盖
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }
}

```

### 5、AbstractAutowireCapableBeanFactory 增加接口 AutowireCapableBeanFactory，实现两个前后置 beanPostProcessor 方法

- 实现接口 AutowireCapableBeanFactory 的两个前后置方法的调用
- createBean 方法增加 initializeBean 流程，即在初始化前后执行 AutowireCapableBeanFactory 接口的前后置方法
 
```java
package cn.itnxd.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.AutowireCapableBeanFactory;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;
import cn.itnxd.springframework.beans.factory.config.BeanReference;
import cn.itnxd.springframework.beans.factory.config.InstantiationStrategy;

import java.lang.reflect.Constructor;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:54
 * @Version 1.0
 * @Description AbstractBeanFactory 的实现类，同样是抽象类，只实现 createBean 方法
 *
 *  增加实现接口AutowireCapableBeanFactory，实现beanPostProcessor
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    // 其他无关内容省略 ...

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

        // 4. 添加到单例缓存 map
        addSingleton(beanName, bean);
        return bean;
    }

    /**
     * 实现父接口 AutowireCapableBeanFactory 的bean初始化前processor
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) throws BeansException {
        Object resultBean = bean;
        // 1. 获取到所有的 BeanPostProcessor
        for(BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            // 2. 依次执行所有的处理方法
            Object dealFinishBean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            if (dealFinishBean == null) {
                // 3. 处理过程中有问题则返回原始bean
                return resultBean;
            }
            resultBean = dealFinishBean;
        }
        return resultBean;
    }

    /**
     * 实现父接口 AutowireCapableBeanFactory 的bean初始化后processor
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) throws BeansException {
        Object resultBean = bean;
        // 1. 获取到所有的 BeanPostProcessor
        for(BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            // 2. 依次执行所有的处理方法
            Object dealFinishBean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            if (dealFinishBean == null) {
                // 3. 处理过程中有问题则返回原始bean
                return resultBean;
            }
            resultBean = dealFinishBean;
        }
        return resultBean;
    }

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

        // TODO 2. bean 初始化方法执行
        invokeInitMethods(beanName, wrapperBean, beanDefinition);

        // 3. BeanPostProcessor后置处理
        wrapperBean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        return wrapperBean;
    }

    /**
     * bean实例化完成后的初始化方法执行
     *
     * @param beanName
     * @param wrapperBean
     * @param beanDefinition
     */
    protected void invokeInitMethods(String beanName, Object wrapperBean, BeanDefinition beanDefinition) {
        // TODO 后面实现
    }

}
```

### 6、DefaultListableBeanFactory 增加接口 ConfigurableListableBeanFactory 实现，主要实现实例化单例 bean 方法

- 实现 preInstantiateSingletons 方法，实现逻辑就是对 beanDefinitionMap 中的 beanName 都做一次 getBean 的调用即可
- 同时实现 ListableBeanFactory 的两个拓展获取 Bean 的方法以及获取所有 beanName 的方法。

```java
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry, ConfigurableListableBeanFactory {

    // 省略旧代码 ...

    /**
     * 实现顶层BeanFactory的根据类型获取bean的方法
     *
     * @param type
     * @return
     * @param <T>
     * @throws BeansException
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> result = new HashMap<>();
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            Class beanClass = beanDefinition.getBeanClass();
            // beanClass 是 type 或者是 type 的子类为true
            if (type.isAssignableFrom(beanClass)) {
                T bean = (T) getBean(beanName);
                result.put(beanName, bean);
            }
        });
        return result;
    }

    /**
     * 实现ListableBeanFactory的方法
     * @return
     */
    @Override
    public String[] getBeanDefinitionNames() {
        Set<String> beanNames = beanDefinitionMap.keySet();
        return beanNames.toArray(new String[beanNames.size()]);
    }

    /**
     * 实现ConfigurableListableBeanFactory的创建单实例方法
     *
     * @throws BeansException
     */
    @Override
    public void preInstantiateSingletons() throws BeansException {
        // 对每个beanName都调用一次get方法即可，从一无所有到全都有
        beanDefinitionMap.keySet().forEach(this::getBean);
    }
}
```

### 7、AbstractRefreshableApplicationContext 继承 AbstractApplicationContext，实现抽象父类没有实现的两个方法 refreshBeanFactory、getBeanFactory

- 本类持有一个 beanFactory，也就是最底层的实现类 DefaultListableBeanFactory
- 实现抽象父类的 refreshBeanFactory 方法，即创建了一个 DefaultListableBeanFactory 保存了起来（这里 ApplicationContext 拥有了 BeanFactory 的所有功能。），并在创建完 beanFactory 后开始装载 beanDefinition 信息到容器。（由子类进行实现）
- 实现抽象父类的 getBeanFactory 方法，即返回保存起来的 beanFactory 实例。


```java
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext{

    private DefaultListableBeanFactory beanFactory;

    /**
     * 实现抽象父类的刷新BeanFactory方法
     *
     * @throws BeansException
     */
    @Override
    protected void refreshBeanFactory() throws BeansException {
        // 1. 创建BeanFactory
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        this.beanFactory = beanFactory;
        // 2. 加载BeanDefinition信息到容器
        loadBeanDefinitions(beanFactory);
    }

    /**
     * 创建一个我们之前实现的最底层的工厂，即DefaultListableBeanFactory。
     *
     * 这里ApplicationContext拥有了BeanFactory的所有功能。
     *
     * @return
     */
    private DefaultListableBeanFactory createBeanFactory() {
        return new DefaultListableBeanFactory();
    }

    /**
     * 加载所有BeanDefinition信息到容器，本方法由子类实现。
     *
     * @param beanFactory
     */
    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory);

    /**
     * 获取创建的BeanFactory
     *
     * @return
     */
    @Override
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
```

### 8、AbstractXmlApplicationContext 实现加载配置文件方法

- 主要实现父类 AbstractRefreshableApplicationContext 定义的加载 BeanDefinition 信息方法
- 创建 BeanDefinitionReader ，即 XmlBeanDefinitionReader 用于读取 xml 配置文件加载 beanDefinition 信息进行保存
- 构造器中注入的 ResourceLoader 是 this，本类的父类是继承过 DefaultResourceLoader，因此这里可以直接注入。
- getConfigLocations 方法由子类进行实现，这个子类就是 **ClassPathXmlApplicationContext** 这个底层 applicationContext 容器。

```java
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext{

    /**
     * 解析配置得到BeanDefinition信息注册到容器中
     *
     * @param beanFactory
     */
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        // 传入BeanDefinitionRegistry 和 ResourceLoader
        // AbstractRefreshableApplicationContext继承AbstractApplicationContext继承DefaultResourceLoader
        // 1. 创建BeanDefinition读取器
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        // 2. 获取配置文件路径地址，由子类实现
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            // 3. 交给读取器获取到Resource进而获取到InputStream进而解析流得到BeanDefinition注册到容器中（DefaultListableBeanFactory）
            beanDefinitionReader.loadBeanDefinitions(configLocations);
        }
    }

    /**
     * 获取配置路径，由子类实现
     *
     * @return
     */
    protected abstract String[] getConfigLocations();
}
```

### 9、ClassPathXmlApplicationContext 实现配置传入以及开启整个容器的 refresh 流程

