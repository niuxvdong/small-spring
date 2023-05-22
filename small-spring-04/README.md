# 第四章 - Bean 实例化完成增加基本属性注入及 Bean 注入

## 一、项目结构

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─itnxd
│  │  │          └─springframework
│  │  │              └─beans
│  │  │                  │  PropertyValue.java
│  │  │                  │  PropertyValues.java
│  │  │                  │
│  │  │                  ├─exception
│  │  │                  │      BeansException.java
│  │  │                  │
│  │  │                  └─factory
│  │  │                      │  BeanFactory.java
│  │  │                      │
│  │  │                      ├─config
│  │  │                      │      BeanDefinition.java
│  │  │                      │      BeanReference.java
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
│  │  └─resources
│  └─test
│      └─java
│          └─cn
│              └─itnxd
│                  └─springframework
│                      │  ApiTest.java
│                      │
│                      └─bean
│                              UserMapper.java
│                              UserService.java
```

## 二、属性注入实现

- 包括基本属性和对象属性
- 对象属性注入会涉及到**循环依赖**，暂不支持，后面支持。

### 1、定义属性 PropertyValue 

- 属性名称和属性值的定义

```java
public class PropertyValue {

    private final String name;

    private final Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
```

### 2、定义属性集合 PropertyValues

- 存储属性的集合类
- 持有 List 的 PropertyValue 集合
- 拥有获取 PropertyValue 的 getPropertyValue 和 添加 getPropertyValue 的 addPropertyValue 方法

```java
public class PropertyValues {

    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    public void addPropertyValue(PropertyValue pv) {
        this.propertyValueList.add(pv);
    }

    /**
     * 将属性集合转化为数组
     * @return
     */
    public PropertyValue[] getPropertyValues() {
        return this.propertyValueList.toArray(new PropertyValue[0]);
    }

    /**
     * 根据属性名获取属性映射pv
     * @param propertyName
     * @return
     */
    public PropertyValue getPropertyValue(String propertyName) {
        return propertyValueList.stream().filter(pv -> pv.getName().equals(propertyName)).findFirst().orElse(null);
    }
}
```

### 3、增加 BeanReference 类标识对象属性

- PropertyValues 存储属性集合
- 对象类型属性，即引用类型使用本类进行标识，本类只保存一个 beanName 属性用于标识即可

```java
public class BeanReference {

    // 使用final修饰防止被更改
    private final String beanName;

    public BeanReference(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
```

### 4、BeanDefinition 增加属性集合 PropertyValues

```java
public class BeanDefinition {

    private Class beanClass;

    // 添加：PropertyValue 属性映射集合
    private PropertyValues propertyValues;

    public BeanDefinition(Class beanClass){
        this.beanClass = beanClass;
        this.propertyValues = new PropertyValues();
    }

    /**
     * BeanDefinition 构造函数增加 PV属性值集合
     *
     * 赋值时判断是否为空进行操作，使属性 propertyValues 一定非空。
     *
     * @param beanClass
     * @param propertyValues
     */
    public BeanDefinition(Class beanClass, PropertyValues propertyValues){
        this.beanClass = beanClass;
        this.propertyValues = propertyValues == null ? new PropertyValues() : propertyValues;
    }

    // get set 方法 省略 ...
}
```

### 5、在 createBean 方法实现中 createBeanInstance 之后增加 applyPropertyValues 属性填充步骤

- `AbstractAutowireCapableBeanFactory`

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    
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
            bean = createBeanInstance(beanName, beanDefinition, args);
            // 2. 对 Bean 进行属性填充
            applyPropertyValues(beanName, beanDefinition, bean);
        } catch (BeansException e) {
            throw new BeansException("初始化Bean失败: ", e);
        }

        // 3. 添加到单例缓存 map
        addSingleton(beanName, bean);
        return bean;
    }

    /**
     * 对实例化完成的 Bean 进行属性填充
     * @param beanName
     * @param beanDefinition
     * @param bean
     */
    protected void applyPropertyValues(String beanName, BeanDefinition beanDefinition, Object bean) {
        try {
            // 1. 获取 BeanDefinition 保存的 PV 集合
            PropertyValue[] propertyValues = beanDefinition.getPropertyValues().getPropertyValues();
            for (PropertyValue pv : propertyValues) {
                String name = pv.getName();
                Object value = pv.getValue();
                // 2. 属性是一个 Bean，递归创建所依赖的 Bean
                if (value instanceof BeanReference) {
                    BeanReference beanReference = (BeanReference) value;
                    value = getBean(beanReference.getBeanName());
                }
                // 3. 将属性 k v 设置到 bean 对象中
                BeanUtil.setFieldValue(bean, name, value);
            }
        } catch (Exception e) {
            throw new BeansException("为 Bean 【" + beanName + "】设置属性失败！");
        }
    }
}
```

## 三、简单测试

- 没有体现出来的类可以到 small-spring-04 中查看

```java
public class ApiTest {

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 注入依赖 BeanDefinition userMapper
        beanFactory.registerBeanDefinition("userMapper", new BeanDefinition(UserMapper.class));

        // 3. 注入 userService (设置属性id和userMapper)
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("id", "10001"));
        // 属性为 Bean 时，注入的是一个 BeanReference，以便于执行相关注入属性操作进行依赖 Bean的逻辑
        propertyValues.addPropertyValue(new PropertyValue("userMapper", new BeanReference("userMapper")));
        beanFactory.registerBeanDefinition("userService", new BeanDefinition(UserService.class, propertyValues));

        // 4. 获取 Bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();
    }
}
```

