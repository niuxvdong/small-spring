# 第十三章 - 增加 @Value 和 @Autowired 属性自动注入

## 一、添加三大注解定义

### 1、@Value

- 使用该注解可以省掉在 xml 中配置 bean 的属性注入
- 可以配置 `${}` 来解析 properties 配置文件中对应的属性值

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Value {

    String value();
}
```

### 2、@Autowired

- 用于注入属性是 Bean 实例

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
public @interface Autowired {

}
```

### 3、@Qualifier

- 结合 @Autowired 注解增加指定 beanName 

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface Qualifier {

	String value() default "";
}
```

## 二、增加属性解析器 Resolver 解析 ${}，添加解析器相关接口

### 1、增加顶层字符串解析器

- 只有一个字符串解析方法，返回解析结果

```java
public interface StringValueResolver {

    /**
     * 字符串解析方法
     *
     * @param strVal
     * @return
     */
    String resolveStringValue(String strVal);
}
```

### 2、增加解析器添加和解析接口

- addEmbeddedValueResolver：解析器，会保存到 AbstractBeanFactory 持有的 embeddedValueResolvers 集合中
- resolveEmbeddedValue：解析接口，对传入值调用解析器返回解析结果
- 两个接口由 AbstractBeanFactory 进行实现(这里不再帖出代码)，就是简单的添加和调用 resolveStringValue 接口，本接口实现由 PropertyPlaceholderConfigurer 的内部类 PlaceholderResolvingStringValueResolver 进行实现
- 跟踪代码会发现实现逻辑就是调用了之前解析 xml 文件中占位符 `${}` 的逻辑代码。

```java
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    // .....

    /**
     * 增加 @Value 注解的值解析器
     *
     * @param valueResolver
     */
    void addEmbeddedValueResolver(StringValueResolver valueResolver);

    /**
     * 增加对传入值调用解析器返回解析结果方法
     *
     * @param value
     * @return
     */
    String resolveEmbeddedValue(String value);
}
```

## 三、特殊的 BeanPostProcessor 即 InstantiationAwareBeanPostProcessor 增加处理注解解析设置

### 1、InstantiationAwareBeanPostProcessor 增加注解解析接口

- 本接口的调用时机是在实例化完成之后，属性设置之前进行调用
- 调用结果是将解析得到的属性键值对 PropertyValue 追加到 BeanDefinition 的 PropertyValues 集合中保存，以供属性设置时候进行调用

```java
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor, BeanFactoryAware {

    /**
     * bean 实例化或初始化之前执行（与 postProcessBeforeInitialization 作用一样，传入参数不一样）
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

    /**
     * 增加对属性 @Value 或 @Autowired 注解的解析设置处理
     *
     * 执行时机：实例化之后，设置属性之前执行。对实例化好的 Bean 设置注解标注的属性
     *
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException;
}

```

### 2、实现类 AutowiredAnnotationBeanPostProcessor 实现注解解析方法

- InstantiationAwareBeanPostProcessor 这个特殊的 BeanPostProcessor 有两个实现类
- DefaultAdvisorAutoProxyCreator：用于向容器中注入代理对象，融入 aop 切面逻辑
- AutowiredAnnotationBeanPostProcessor：用于在实例化之后解析注解设置到 PropertyValues 属性集合便于属性注入
- 这个 BeanPostProcessor 会在包扫描器 ClassPathBeanDefinitionScanner 中手动注入容器。
- 对于处理 aop 切面逻辑所定义的接口方法，为了保证职责单合一，我们这里重写后只需要返回 null，即可在生命周期中根据是否为 null 判断是否需要代理对象。

```java
public class AutowiredAnnotationBeanPostProcessor  implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    /**
     * 实现 InstantiationAwareBeanPostProcessor 特殊的 BeanPostProcessor 新增的接口
     * 增加对属性 @Value 或 @Autowired 注解的解析设置处理
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        // 1、处理 @Value 注解
        Class<?> clazz = bean.getClass();
        // 处理 cglib 的真实 class
        clazz = isCglibClass(clazz) ? clazz.getSuperclass() : clazz;

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Value valueAnnotation = field.getAnnotation(Value.class);
            if (valueAnnotation != null) {
                String value = valueAnnotation.value();
                // 使用容器中字符串解析器 解析注解的 value 属性返回解析结果
                value = beanFactory.resolveEmbeddedValue(value);
                // 为 bean 设置解析后的属性值（配置文件中获取占位符属性对应值）
                BeanUtil.setFieldValue(bean, field.getName(), value);
            }
        }
        // 2、处理 @Autowired 以及配合使用的 @Qualifier
        for (Field field : fields) {
            Autowired autowiredAnnotation = field.getAnnotation(Autowired.class);
            if (autowiredAnnotation != null) {
                Class<?> fieldType = field.getType();
                String dependentBeanName = null;
                Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);
                Object dependentBean = null;
                if (qualifierAnnotation != null) {
                    // 获取 Qualifier 注解指定的 beanName
                    dependentBeanName = qualifierAnnotation.value();
                    // 指定 beanName 和 class
                    dependentBean = beanFactory.getBean(dependentBeanName, fieldType);
                } else {
                    // 没有 Qualifier 注解直接按照类型获取
                    dependentBean = beanFactory.getBean(fieldType);
                }
                BeanUtil.setFieldValue(bean, field.getName(), dependentBean);
            }
        }
        return pvs;
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

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 职责单一：本类只处理两个注解的解析，不处理 DefaultAdvisorAutoProxyCreator 已经实现的 aop 切面逻辑
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }
}
```

### 3、实现类 DefaultAdvisorAutoProxyCreator 增加接口实现

- 职责分离，本类只处理 aop 切面代理对象的生成，本方法直接返回不做处理。

```java
public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor {

    // ...... 

    /**
     * 职责分离，本类只处理 aop 切面代理对象的生成。
     * 本方法直接返回不做处理。
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        return pvs;
    }
}
```

## 四、手动向容器中添加支持处理三大注解的解析器 Resolver

- PropertyPlaceholderConfigurer 是需要在 xml 中进行注入的
- postProcessBeanFactory 方法最后增加手动注入 Resolver 到 AbstractBeanFactory 持有的 embeddedValueResolvers 集合中
- 解析器 Resolver 会在 AutowiredAnnotationBeanPostProcessor 解析注解时候被调用，这个特殊的 BeanPostProcessor 是在 ClassPathBeanDefinitionScanner 包扫描时被注册
- 这里解析器的解析方法 resolveStringValue 也是调用的 PropertyPlaceholderConfigurer 解析 `${}` 的逻辑。

```java
public class PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {


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
                    // 解析占位符从配置中拿到对应值返回
                    value = resolvePlaceholder((String) value, properties);
                    // 将属性k v 对添加到 propertyValues 里
                    // 这里动态向 propertyValues 添加元素，会造成死循环吗？不会，会在 index 的判断下跳出循环。
                    propertyValues.addPropertyValue(new PropertyValue(propertyValue.getName(), value));
                }
            }
        }

        // 增加：往容器中添加字符解析器，供解析 @Value 注解使用
        StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(properties);
        beanFactory.addEmbeddedValueResolver(valueResolver);
    }

    /**
     * 增加占位符解析器，添加到容器中用来解析 @Value 注解
     */
    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

        // 持有 properties 配置文件
        private final Properties properties;

        public PlaceholderResolvingStringValueResolver(Properties properties) {
            this.properties = properties;
        }

        /**
         * 将传入的 ${} 进行解析拿到真实值返回
         * @param strVal
         * @return
         * @throws BeansException
         */
        public String resolveStringValue(String strVal) throws BeansException {
            return resolvePlaceholder(strVal, properties);
        }
    }
}
```

## 五、手动向容器中添加支持处理三大注解的 BeanPostProcessor 的 BeanDefinition 信息

- doScan 方法最后增加 BeanDefinition 的注册，将 AutowiredAnnotationBeanPostProcessor 注册到容器中
- 此时容器中有了支持处理三大注解解析的 BeanPostProcessor 和 解析器 Resolver

```java
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

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

        // 增加：想容器注册处理 @Autowired 和 @Value 注解的 BeanPostProcessor
        registry.registerBeanDefinition("cn.itnxd.springframework.context.annotation.internalAutowiredAnnotationProcessor", new BeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
    }
}
```

## 六、将注解解析能力融入到 Bean 生命周期

- 增加 applyBeanPostProcessorsBeforeApplyingPropertyValues 方法来处理注解解析
- 将解析结果追加到 PropertyValues 集合，以供 applyPropertyValues 进行属性填充

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

    // ...........

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            // 增加：判断是否是代理对象(是则直接返回代理对象,不继续走下面流程)
            bean = resolveBeforeInstantiation(beanName, beanDefinition);
            if (bean != null) return bean;

            // 1. 根据 BeanDefinition 创建 Bean
            bean = createBeanInstance(beanName, beanDefinition, args);

            // 增加：实例化之后，设置属性之前通过特殊的 BeanPostProcessor 处理 @value 和 @Autowired 注解的解析
            applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName, bean, beanDefinition);

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

    /**
     * 实例化之后，设置属性之前通过特殊的 BeanPostProcessor 处理 @value 和 @Autowired 注解的解析
     * 同样是将新的属性对 propertyValue 增加到 propertyValues 属性集合，统一在 applyPropertyValues 时候进行赋值，这里赋值是新值覆盖旧值
     *
     * @param beanName
     * @param bean
     * @param beanDefinition
     */
    protected void applyBeanPostProcessorsBeforeApplyingPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {

        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                // 特殊的 BeanPostProcessor 则执行 postProcessPropertyValues 方法进行解析
                PropertyValues pvs = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessPropertyValues(beanDefinition.getPropertyValues(), bean, beanName);
                if (pvs != null) {
                    for (PropertyValue propertyValue : pvs.getPropertyValues()) {
                        // 追加到 BeanDefinition 集合中进行保存
                        beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
                    }
                }
            }
        }
    }
}
```

## 七、增加普通属性注入和 bean 注入测试

### xml 配置

- 只需添加包扫描路径和 PropertyPlaceholderConfigurer 并指定 properties 配置文件位置即可

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

    <bean class="cn.itnxd.springframework.beans.factory.PropertyPlaceholderConfigurer">
        <property name="location" value="classpath:user.properties" />
    </bean>

</beans>
```

### 添加测试

- 其他相关测试代码请查看项目代码

```java
public class ApiTest {

    @Test
    public void test_scan() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");

        UserService userService = applicationContext.getBean("userService", UserService.class);

        userService.getUserInfo();

        /*
        查询用户信息: itnxd
        car: cn.itnxd.springframework.bean.Car$$EnhancerByCGLIB$$aaaf2a2f@51efea79
        */
    }
}
```