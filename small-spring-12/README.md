# 第十二章 - 支持包扫描和 ${} 占位符配置解析

## 一、添加特殊的 BeanFactoryPostProcessor 处理占位符的解析设置

- PropertyPlaceholderConfigurer 是一个 BeanFactoryPostProcessor，可以在容器加载完所有 BeanDefinition 后提供对其的修改，即对属性值为占位符 `${}` 的属性，读取 properties 配置文件对应值后进行赋值
- 拿到配置文件对应的属性值后，没有进行替换而是直接 propertyValues.addPropertyValue，由于我们有了 index 索引的判断因此不会造成死循环发生
- 创建完 bean 实例后，为属性赋值时由于我们解析完成后的属性键值对顺序是在未解析的占位符之后的，因此可以被配置文件中的值进行覆盖

```java
public class PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {

    // 占位符前缀
    public static final String PLACEHOLDER_PREFIX = "${";
    // 占位符后缀
    public static final String PLACEHOLDER_SUFFIX = "}";

    // yml 或 properties 配置文件路径
    private String location;

    /**
     * 实现 BeanFactoryPostProcessor 在解析完所有 BeanDefinition 信息后提供对 BeanDefinition 的修改
     * 提供对占位符设置到 BeanDefinition 的功能。
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 从配置文件加载属性信息
        Properties properties = loadProperties();

        // 获取解析xml配置得到的所有 BeanDefinition
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            // 拿到 BeanDefinition 的属性集合
            PropertyValues propertyValues = beanDefinition.getPropertyValues();
            for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
                // 拿到属性值
                Object value = propertyValue.getValue();
                // 只能解析 String 类型的属性值
                if (value instanceof String) {
                    String strVal = (String) value;
                    StringBuffer buf = new StringBuffer(strVal);
                    int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
                    int endIndex = strVal.indexOf(PLACEHOLDER_SUFFIX);
                    // ${} 合法
                    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                        // key 去掉 ${} 得到属性 key
                        String propKey = strVal.substring(startIndex + 2, endIndex);
                        // 从properties配置文件中得到对应的 属性 value
                        String propVal = properties.getProperty(propKey);
                        // 将 ${xxx} 替换为 对应的配置文件中的值
                        buf.replace(startIndex, endIndex + 1, propVal);
                        // 将属性k v 对添加到 propertyValues 里
                        // 这里动态向 propertyValues 添加元素，会造成死循环吗？不会，会在 index 的判断下跳出循环。
                        propertyValues.addPropertyValue(new PropertyValue(propertyValue.getName(), buf.toString()));
                    }
                }
            }
        }
    }

    private Properties loadProperties() {
        try {
            // 通过资源加载器加载资源
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource(location);
            // 使用 Properties 工具包加载配置信息
            Properties properties = new Properties();
            properties.load(resource.getInputStream());
            return properties;
        } catch (IOException e) {
            throw new BeansException("不能加载 properties 配置文件信息", e);
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
```

## 二、添加 Component 和 Scope 注解

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

    String value() default "singleton";
}


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    String value() default "";
}
```

## 三、添加包扫描以及扫描完成注册 Bean 定义信息

### 1、扫描指定包下被 Component 注解标注的类

```java
public class ClassPathScanningCandidateComponentProvider {

    /**
     * 扫描指定包下被 Component 注解标注的类
     * @param basePackage
     * @return
     */
    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
        // 扫描有org.springframework.stereotype.Component注解的类
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(basePackage, Component.class);
        for (Class<?> clazz : classes) {
            BeanDefinition beanDefinition = new BeanDefinition(clazz);
            candidates.add(beanDefinition);
        }
        return candidates;
    }
}
```

### 2、扫描到的 Beandefinition 注册到容器中


```java
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

    // 持有 BeanDefinition 注册中心，用来注册到 DefaultListableBeanFactory 的 beanDefinitionMap
    private BeanDefinitionRegistry registry;

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void doScan(String... basePackages) {
        for (String basePackage : basePackages) {
            // 调用父类依次扫描
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                // 解析 bean 的作用域 Scope 注解
                String beanScope = resolveBeanScope(candidate);
                if (StrUtil.isNotEmpty(beanScope)) {
                    candidate.setScope(beanScope);
                }
                // 生成 bean 的名称
                String beanName = determineBeanName(candidate);
                // 注册 BeanDefinition
                registry.registerBeanDefinition(beanName, candidate);
            }
        }
    }

    /**
     * 获取 bean 的作用域
     *
     * @param beanDefinition
     * @return
     */
    private String resolveBeanScope(BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Scope scope = beanClass.getAnnotation(Scope.class);
        if (scope != null) {
            return scope.value();
        }
        return "";
    }


    /**
     * 生成 bean 的名称
     *
     * @param beanDefinition
     * @return
     */
    private String determineBeanName(BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Component component = beanClass.getAnnotation(Component.class);
        // 获取 Component 属性作为 beanName
        String value = component.value();
        if (StrUtil.isEmpty(value)) {
            // 否则获取 class 首字母小写
            value = StrUtil.lowerFirst(beanClass.getSimpleName());
        }
        return value;
    }
}
```

## 四、使用 dom4j 重写解析 xml 文件并加入 component-scan 包扫描属性解析

- 具体代码查看 XmlBeanDefinitionReader

## 五、增加 ${} 占位符和包扫描测试类

### 1、包扫描测试

#### xml 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	         http://www.springframework.org/schema/beans/spring-beans.xsd
		 http://www.springframework.org/schema/context
		 http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <context:component-scan base-package="cn.itnxd.springframework.bean"/>

</beans>
```

#### 测试

```java
@Test
public void test_scan() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");

    UserService userService = applicationContext.getBean("userService", UserService.class);

    userService.getUserInfo();
}
```

### 2、占位符测试

#### xml 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>

    <bean id="userService" class="cn.itnxd.springframework.bean.UserServiceImpl">
        <property name="username" value="${username}"/>
    </bean>

    <bean class="cn.itnxd.springframework.beans.factory.PropertyPlaceholderConfigurer">
        <property name="location" value="classpath:user.properties" />
    </bean>

</beans>
```

#### properties 配置文件

```properties
username=itnxd
```

#### 测试

```java
@Test
public void test_PropertyPlaceholderConfigurer() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

    UserService userService = applicationContext.getBean("userService", UserService.class);

    userService.getUserInfo(); // itnxd
}
```