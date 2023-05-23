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

#### 