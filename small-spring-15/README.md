# 第十五章 - 添加数据类型转换器并融入 Bean 生命周期

## 一、添加数据类型转换器实现

### 1、添加三种类型转换器接口

#### Converter

- 只能实现一种类型到另一种类型的转换，即一对一

```java
public interface Converter<S, T> {

    /**
     * 类型转换 S -> T
     */
    T convert(S source);
}
```

#### ConverterFactory

- 一对一的升级，一对多，由工厂实现可以获取要转换类型的子类的转换器的工厂

```java
public interface ConverterFactory<S, R> {

    /**
     * 获取转换器方法。
     * 特点：支持一种 S 多种 T, T 只要是 R 或子类即可。例如将 String -> Number
     *
     * @param targetType
     * @return
     * @param <T>
     */
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
```

#### GenericConverter

- 一般转换器，即无需指定哪种类型转换成哪种类型
- 持有 ConvertiblePair 类型对，即原始类型和要转换目标类型的映射的封装

```java
public interface GenericConverter {

    // 持有获取类型对的接口
    Set<ConvertiblePair> getConvertibleTypes();

    // 持有核心转换方法的接口
    Object convert(Object source, Class<?> sourceType, Class<?> targetType);

    /**
     * 内部类：保存类型对，源类型与目标类型对
     */
    public static final class ConvertiblePair {

        private final Class<?> sourceType;

        private final Class<?> targetType;

        public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public Class<?> getSourceType() {
            return this.sourceType;
        }

        public Class<?> getTargetType() {
            return this.targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || obj.getClass() != ConvertiblePair.class) {
                return false;
            }
            ConvertiblePair other = (ConvertiblePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }

        @Override
        public int hashCode() {
            return this.sourceType.hashCode() * 31 + this.targetType.hashCode();
        }
    }
}
```

### 2、添加转换服务类 ConversionService

- 既然是服务类，也就是转换功能的核心入口所在，转换都通过本类来完成
- 定义一个判断是否可以转换的接口，一个核心转换接口

```java
public interface ConversionService {

    /**
     * 判断是源类型和目标类型能否转换
     * @param sourceType
     * @param targetType
     * @return
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /**
     * 将源类型转换为目标类型返回
     * @param source
     * @param targetType
     * @return
     * @param <T>
     */
    <T> T convert(Object source, Class<T> targetType);
}
```

### 3、添加转换器注册中心

- 注册中心用于注册三种转换器保存起来
- 即我们刚开始定义的三种转换器类型：converter、converterFactory、GenericConverter

```java
public interface ConverterRegistry {

    /**
     * 添加转化器
     * @param converter
     */
    void addConverter(Converter<?, ?> converter);

    /**
     * 添加转换工厂
     * @param converterFactory
     */
    void addConverterFactory(ConverterFactory<?, ?> converterFactory);

    /**
     * 添加 GenericConverter 转换器
     * @param converter
     */
    void addConverter(GenericConverter converter);
}
```

### 4、添加 ConversionService 实现类 GenericConversionService 兼容三种转换器

- 实现顶层转换服务接口以及转换器注册中心
- 持有一个转换类型对 ConvertiblePair 和 转换器 GenericConverter 的映射即可
- 实现转换服务的判断是否可以转换和核心转换接口
- GenericConverter 可以视为顶层转换器接口，Converter 和 ConverterFactory 都是通过实现 GenericConverter 接口拿到适配器 adapter 进行保存的
- 本类持有的 converters 集合最终添加的就是 ConverterAdapter、ConverterFactoryAdapter、GenericConverter 三种
- 核心转换方法 convert 都是通过调用 adapter 的 convert 方法，而 adapter 适配器的 convert 就是适配了三种转换器的实现，调用 `converter.convert(source)` 或者 `converterFactory.getConverter(targetType).convert(source)` 或者是 `genericConverter.convert(source, sourceType, targetType)`

```java
public class GenericConversionService implements ConversionService, ConverterRegistry {

    // 持有转换类型对 ConvertiblePair 和 一般转换器 GenericConverter 的映射集合
    // 可以保存 Converter（通过adapter实现GenericConverter接口）、ConverterFactory（通过adapter实现GenericConverter接口）、GenericConverter 三种
    private final Map<ConvertiblePair, GenericConverter> converters = new HashMap<>();

    /**
     * 实现 ConversionService 的判断接口
     * @param sourceType
     * @param targetType
     * @return
     */
    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        // 获取源类型到目标类型的转换器
        GenericConverter converter = getConverter(sourceType, targetType);
        // 若有这种转换器则说明可以转换
        return converter != null;
    }

    /**
     * 实现 ConversionService 的核心转换接口
     * @param source
     * @param targetType
     * @return
     * @param <T>
     */
    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        Class<?> sourceType = source.getClass();
        // 将目标类型转换成包装类型
        targetType = (Class<T>) BasicType.wrap(targetType);
        // 获取转换器
        GenericConverter converter = getConverter(sourceType, targetType);
        // 使用转换器进行转换
        return (T) converter.convert(source, sourceType, targetType);
    }

    /**
     * 实现 ConverterRegistry 的添加转换器接口
     * @param converter
     */
    @Override
    public void addConverter(Converter<?, ?> converter) {
        // 根据转换器泛型接口获取转换类型对
        ConvertiblePair typeInfo = getRequiredTypeInfo(converter);
        // 通过转换类型对和转换器获取转换器适配器 adapter
        ConverterAdapter converterAdapter = new ConverterAdapter(typeInfo, converter);
        for (ConvertiblePair convertibleType : converterAdapter.getConvertibleTypes()) {
            // 遍历适配器保存的类型转换对，保存到 转换对和 GenericConverter 映射集合中
            // 这里的适配器是 GenericConverter 的实现类
            converters.put(convertibleType, converterAdapter);
        }
    }

    /**
     * 实现 ConverterRegistry 的添加转换器工厂接口
     * @param converterFactory
     */
    @Override
    public void addConverterFactory(ConverterFactory<?, ?> converterFactory) {
        // 根据转换器工厂泛型接口获取转换类型对
        ConvertiblePair typeInfo = getRequiredTypeInfo(converterFactory);
        // 通过转换类型对和转换器工厂获取转换器工厂适配器 adapter
        ConverterFactoryAdapter converterFactoryAdapter = new ConverterFactoryAdapter(typeInfo, converterFactory);
        for (ConvertiblePair convertibleType : converterFactoryAdapter.getConvertibleTypes()) {
            // 遍历适配器保存的类型转换对，保存到 转换对和 GenericConverter 映射集合中
            // 这里的适配器也是 GenericConverter 的实现类
            converters.put(convertibleType, converterFactoryAdapter);
        }
    }

    /**
     * 实现 ConverterRegistry 的添加一般转换器接口
     * @param converter
     */
    @Override
    public void addConverter(GenericConverter converter) {
        for (ConvertiblePair convertibleType : converter.getConvertibleTypes()) {
            // 参数是 GenericConverter 顶层接口，直接添加
            converters.put(convertibleType, converter);
        }
    }

    /**
     * 根据转换器获取转换类型对
     * @param object
     * @return
     */
    private ConvertiblePair getRequiredTypeInfo(Object object) {
        // 获取泛型参数
        Type[] types = object.getClass().getGenericInterfaces();
        ParameterizedType parameterized = (ParameterizedType) types[0];
        Type[] actualTypeArguments = parameterized.getActualTypeArguments();
        // 第一个泛型参数和第二个泛型参数
        Class<?> sourceType = (Class<?>) actualTypeArguments[0];
        Class<?> targetType = (Class<?>) actualTypeArguments[1];
        // 根据两个泛型参数创建转换对返回
        return new ConvertiblePair(sourceType, targetType);
    }

    /**
     * 获取源类型到目的类型的一般转换器
     * @param sourceType
     * @param targetType
     * @return
     */
    protected GenericConverter getConverter(Class<?> sourceType, Class<?> targetType) {
        // 如果是基本类型返回对应的包装类型，包括包装类型的父类，例如 int -> Integer -> Number
        List<Class<?>> sourceCandidates = getClassHierarchy(sourceType);
        List<Class<?>> targetCandidates = getClassHierarchy(targetType);
        for (Class<?> sourceCandidate : sourceCandidates) {
            for (Class<?> targetCandidate : targetCandidates) {
                ConvertiblePair convertiblePair = new ConvertiblePair(sourceCandidate, targetCandidate);
                // 两层循环进行排列组合判断 converter 集合是由有这种转换器，有则返回
                GenericConverter converter = converters.get(convertiblePair);
                if (converter != null) {
                    return converter;
                }
            }
        }
        return null;
    }

    /**
     * 获取传入类型的包装类型 int -> integer，由于有循环因此最终获取到的是 integer、Number
     * @param clazz
     * @return
     */
    private List<Class<?>> getClassHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        clazz = BasicType.wrap(clazz);
        while (clazz != null) {
            hierarchy.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return hierarchy;
    }

    /**
     * 转换器适配器，实现 GenericConverter 接口。可以被保存到本类持有的 converters 集合
     * 包装转换对和转换器映射。
     */
    private static final class ConverterAdapter implements GenericConverter {

        // 持有转换类型对
        private final ConvertiblePair typeInfo;

        // 持有转换器
        private final Converter<Object, Object> converter;

        public ConverterAdapter(ConvertiblePair typeInfo, Converter<?, ?> converter) {
            this.typeInfo = typeInfo;
            this.converter = (Converter<Object, Object>) converter;
        }

        /**
         * 实现获取父类转换对接口
         * @return
         */
        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(typeInfo);
        }

        /**
         * 实现父类核心转换方法
         * @param source
         * @param sourceType
         * @param targetType
         * @return
         */
        @Override
        public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
            return converter.convert(source);
        }
    }

    /**
     * 转换器工厂适配器，实现 GenericConverter 接口。可以被保存到本类持有的 converters 集合
     * 包装转换对和转换工厂映射。
     */
    private static final class ConverterFactoryAdapter implements GenericConverter {

        // 持有转换类型对
        private final ConvertiblePair typeInfo;

        // 持有转换工厂
        private final ConverterFactory<Object, Object> converterFactory;

        public ConverterFactoryAdapter(ConvertiblePair typeInfo, ConverterFactory<?, ?> converterFactory) {
            this.typeInfo = typeInfo;
            this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(typeInfo);
        }

        @Override
        public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {
            return converterFactory.getConverter(targetType).convert(source);
        }
    }
}
```

### 5、添加 DefaultConversionService 用来自动注入默认的转换器

- 构造器中进行添加默认转换器逻辑
- 最终都保存到了 GenericConversionService 持有的 converters 集合

```java
public class DefaultConversionService extends GenericConversionService{

    public DefaultConversionService() {
        // 父类继承自注册中心 ConverterRegistry
        addDefaultConverters(this);
    }

    /**
     * 本类为静态方法，不能直接调用添加转换工厂方法，但可以通过转入转换器注册中心来调用
     * @param converterRegistry
     */
    public static void addDefaultConverters(ConverterRegistry converterRegistry) {
        // 默认转换工厂
        converterRegistry.addConverterFactory(new StringToNumberConverterFactory());
        // 其他默认转换工厂....
    }
}
```

### 6、添加转换器测试类

```java
public class ApiTest {

    // 简单实现 Converter<String, Integer> 的转换器
    @Test
    public void testStringToIntegerConverter() throws Exception {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Integer num = converter.convert("8888");

        System.out.println(num);
    }

    // 简单实现 ConverterFactory<String, Number> 的转换器
    @Test
    public void testStringToNumberConverterFactory() throws Exception {
        StringToNumberConverterFactory converterFactory = new StringToNumberConverterFactory();

        Converter<String, Integer> stringToIntegerConverter = converterFactory.getConverter(Integer.class);
        Integer intNum = stringToIntegerConverter.convert("8888");

        System.out.println(intNum);

        Converter<String, Long> stringToLongConverter = converterFactory.getConverter(Long.class);
        Long longNum = stringToLongConverter.convert("8888");

        System.out.println(longNum);
    }

    // 简单实现 GenericConverter 接口的转换器（需实现判断和转换两个接口）
    @Test
    public void testGenericConverter() throws Exception {
        StringToBooleanConverter converter = new StringToBooleanConverter();

        Boolean flag = (Boolean) converter.convert("true", String.class, Boolean.class);

        System.out.println(flag);
    }

    // 通过转换服务类 GenericConversionService 进行转换（可以添加三种转换器进行转换服务）
    @Test
    public void testGenericConversionService() throws Exception {
        GenericConversionService conversionService = new GenericConversionService();
        // 向注册中心添加 converter
        conversionService.addConverter(new StringToIntegerConverter());

        Integer intNum = conversionService.convert("8888", Integer.class);
        Boolean canConvert = conversionService.canConvert(String.class, Integer.class);

        System.out.println(intNum + " " + canConvert);

        // 向注册中心添加 ConverterFactory
        conversionService.addConverterFactory(new StringToNumberConverterFactory());
        Boolean canConvert1 = conversionService.canConvert(String.class, Long.class);
        Long longNum = conversionService.convert("8888", Long.class);

        System.out.println(longNum + " " + canConvert1);

        // 向注册中心添加 GenericConverter
        conversionService.addConverter(new StringToBooleanConverter());
        Boolean canConvert2 = conversionService.canConvert(String.class, Boolean.class);
        Boolean flag = conversionService.convert("true", Boolean.class);

        System.out.println(flag + " " + canConvert2);
    }
}
```

## 二、将转换服务融入 Bean 生命周期

### 1、添加 ConversionServiceFactoryBean 向容器注册 conversionService

- 通过工厂 Bean 的方式向容器注册转换服务
- 类型转换服务，可以在 xml 中配置注入或通过注解注入容器，beanName 为 conversionService
- 本类还实现了 InitializingBean 接口，即在 bean 创建完成属性设置完属性之后进行初始化方法 initializeBean 执行时进行 afterPropertiesSet 操作
- afterPropertiesSet 有两个操作：
    - 一个是对 conversionService 赋值，赋值为 DefaultConversionService（自动添加默认的转换器）
    - 一个是将在 ConversionServiceFactoryBean 这个 bean 属性赋值时期进行设置的 converters 注册到 GenericConversionService 持有的 converters 集合中
- 整体流程就是：先创建 Bean（这里会先涉及到属性设置为 converters 进行赋值，然后 InitializingBean 为 conversionService 赋值，再将赋值的 converters 注册到 conversionService 持有的 converters集合），创建完成再执行工厂 Bean 的对象替换，最终容器中就是 conversionService，而不是 ConversionServiceFactoryBean


```java
public class ConversionServiceFactoryBean implements FactoryBean<ConversionService>, InitializingBean {

    // 持有类型转换器集合（Converter、ConverterFactory、GenericConverter）
    private Set<?> converters;

    // 持有 ConversionService 转换服务的实现类
    private GenericConversionService conversionService;

    /**
     * 实现 FactoryBean 的 getObj 方法
     * @return
     * @throws Exception
     */
    @Override
    public ConversionService getObject() throws Exception {
        return this.conversionService;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * set 方法，在属性赋值时期进行设置 converters
     * @param converters
     */
    public void setConverters(Set<?> converters) {
        this.converters = converters;
    }

    /**
     * 实现 InitializingBean 接口方法（bean创建完成属性设置完属性之后进行初始化方法执行initializeBean）
     *
     * 初始化方法中进行设置
     * @throws BeansException
     */
    @Override
    public void afterPropertiesSet() throws BeansException {
        // 初始化方法中为持有的属性 转换服务 赋值为 DefaultConversionService（自动添加默认的转换器）
        conversionService = new DefaultConversionService();
        // 注册属性赋值过程注册的 converters 到 conversionService中
        registerConverters(converters, conversionService);
    }

    /**
     * 将设置属性时期添加的 converters 保存到 converter 注册中心中
     * @param converters
     * @param converterRegistry
     */
    private void registerConverters(Set<?> converters, ConverterRegistry converterRegistry) {
        if (converters != null) {
            for (Object converter : converters) {
                if (converter instanceof GenericConverter) {
                    converterRegistry.addConverter((GenericConverter) converter);
                } else if (converter instanceof Converter<?, ?>) {
                    converterRegistry.addConverter((Converter<?, ?>) converter);
                } else if (converter instanceof ConverterFactory<?, ?>) {
                    converterRegistry.addConverterFactory((ConverterFactory<?, ?>) converter);
                } else {
                    throw new IllegalArgumentException("类型转换器 converter 必须实现以下三大接口：Converter, ConverterFactory, GenericConverter");
                }
            }
        }
    }
}
```

### 2、refresh 流程在实例化前先进行 conversionService 类型转换服务注册

- 在 Bean 实例化之前先进行类型转换器注册，以便于 bean 实例化时候再属性设置阶段可以通过类型转换器对属性值进行类型转换
- conversionService，可以在 xml 或 注解注入 ConversionServiceFactoryBean，实际的 Bean 就是 ConversionService
- 注册的类型转换器最终保存到 AbstractBeanFactory 持有的 conversionService 转换服务

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

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
        //beanFactory.preInstantiateSingletons();
        // 修改：（二合一）注册类型转换器 和 提前实例化单例bean
        finishBeanFactoryInitialization(beanFactory);

        // 9. 发布事件：容器refresh完成事件
        finishRefresh();
    }

    /**
     * 在实例化 bean 之前先进行类型转换器注册
     * @param beanFactory
     */
    private void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        // 设置类型转换器
        if (beanFactory.containsBean("conversionService")) {
            Object conversionService = beanFactory.getBean("conversionService");
            if (conversionService instanceof ConversionService) {
                // 通过 BeanFactory 保存到 AbstractBeanFactory 持有的 conversionService 中
                beanFactory.setConversionService((ConversionService) conversionService);
            }
        }
        // 提前实例化单例bean
        beanFactory.preInstantiateSingletons();
    }

    // ....................
}
```

### 3、在两处属性设置时期对属性添加类型转换逻辑

#### applyPropertyValues 阶段

- AbstractAutowireCapableBeanFactory 中的逻辑
- 如果属性不是 bean 就会设计到类型转换
- 容器中的转换服务存在，并且可以对这种类型进行转换时才进行转换，否则直接跳过即可

```java
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
            } else {
                // 属性不是 bean，则进行类型转换后再设置属性值
                Class<?> sourceType = value.getClass();
                // 获取到 bean 中属性的类型
                Class<?> targetType = (Class<?>) TypeUtil.getFieldType(bean.getClass(), name);
                // 获取到注册到容器的 类型转换服务
                ConversionService conversionService = getConversionService();
                if (conversionService != null) {
                    if (conversionService.canConvert(sourceType, targetType)) {
                        // 非空且可以进行转换则调用转换服务进行转换
                        value = conversionService.convert(value, targetType);
                    }
                }
            }
            // 3. 将属性 k v 设置到 bean 对象中
            BeanUtil.setFieldValue(bean, name, value);
        }
    } catch (Exception e) {
        throw new BeansException("为 Bean 【" + beanName + "】设置属性失败！");
    }
}
```

#### @Value 属性注入阶段

- AutowiredAnnotationBeanPostProcessor 中的逻辑
- 容器中的转换服务存在，并且可以对这种类型进行转换时才进行转换，否则直接跳过即可

```java
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
            Object value = valueAnnotation.value();
            // 使用容器中字符串解析器 解析注解的 value 属性返回解析结果
            value = beanFactory.resolveEmbeddedValue((String) value);

            // 增加：类型转换能力
            Class<?> sourceType = value.getClass();
            Class<?> targetType = (Class<?>) TypeUtil.getType(field);
            ConversionService conversionService = beanFactory.getConversionService();
            if (conversionService != null) {
                if (conversionService.canConvert(sourceType, targetType)) {
                    value = conversionService.convert(value, targetType);
                }
            }

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
```

### 4、对容器增加的类型转换功能进行测试

#### xml 配置

- 要注入转换服务工厂 Bean：ConversionServiceFactoryBean
- 以及用户实现的转换器 ConvertersFactoryBean
- 转换服务工厂会注入用户添加的 converters

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	         http://www.springframework.org/schema/beans/spring-beans.xsd
		 http://www.springframework.org/schema/context
		 http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <bean id="userService" class="cn.itnxd.springframework.bean.UserServiceImpl">
        <property name="success" value="true"/>
    </bean>

    <bean id="conversionService" class="cn.itnxd.springframework.context.support.ConversionServiceFactoryBean">
        <property name="converters" ref="converters"/>
    </bean>

    <bean id="converters" class="cn.itnxd.springframework.converter.ConvertersFactoryBean"/>

</beans>
```

#### 用户通过工厂 Bean 向容器中添加需要的转换器

- 三种类型的转换器都可以添加到 set 集合注入容器
- 这些由用户添加的转化器都会被 ConversionServiceFactoryBean 进行注册到容器中来供 转换服务进行使用

```java
public class ConvertersFactoryBean implements FactoryBean<Set<?>> {

    @Override
    public Set<?> getObject() throws Exception {
        Set<Object> converters = new HashSet<>();
        StringToBooleanConverter stringToBooleanConverter = new StringToBooleanConverter();
        converters.add(stringToBooleanConverter);
        return converters;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
```

#### 测试

- 这里进行测试，这时候容器中有转换服务 conversionService
- 有用户添加的转换器 StringToBooleanConverter，以及 conversionService 初始化阶段自动注入的 默认转换器 DefaultConversionService 中定义的 StringToNumberConverterFactory 转换工厂

```java
@Test
public void testConversionService() throws Exception {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:scan.xml");

    UserService userService = applicationContext.getBean("userService", UserService.class);

    userService.getUserInfo(); // success: true
}
```