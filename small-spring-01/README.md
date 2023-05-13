# 第一章 - 实现最简单的 Bean 容器

## 一、项目结构

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─itnxd
│  │  │          └─springframework
│  │  │                  BeanDefinition.java
│  │  │                  BeanFactory.java
│  │  │
│  │  └─resources
```

- BeanDefinition：本类存放 bean 的一些定义信息，这里我们只存放 bean 这个对象 Object.
- BeanFactory：bean 工厂，提供获取 bean 的能力，最简单的实现就是使用一个 map 保存 beanName 和 bean 对象的映射即可。

## 二、简单测试

```java
public class ApiTest {

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        BeanFactory beanFactory = new BeanFactory();

        // 2. 注入 Bean
        BeanDefinition beanDefinition = new BeanDefinition(new UserService());
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        // 3. 获取 Bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();
    }
}
```