package cn.itnxd.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.PropertyValues;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.AutowireCapableBeanFactory;
import cn.itnxd.springframework.beans.factory.BeanFactoryAware;
import cn.itnxd.springframework.beans.factory.DisposableBean;
import cn.itnxd.springframework.beans.factory.InitializingBean;
import cn.itnxd.springframework.beans.factory.config.*;
import cn.itnxd.springframework.core.convert.ConversionService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:54
 * @Version 1.0
 * @Description AbstractBeanFactory 的实现类，同样是抽象类，只实现 createBean 方法
 *
 *  增加实现接口AutowireCapableBeanFactory，实现beanPostProcessor
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

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
            bean = createBeanInstance(beanName, beanDefinition, args);

            // 增加：实例化之后，设置属性之前通过特殊的 BeanPostProcessor 处理 @value 和 @Autowired 注解的解析
            applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName, bean, beanDefinition);

            // 2. 对 Bean 进行属性填充
            applyPropertyValues(beanName, beanDefinition, bean);
            // 3. bean实例化完成，执行初始化方法以及在初始化前后分别执行BeanPostProcessor
            bean = initializeBean(beanName, beanDefinition, bean);
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

    /**
     * 废弃：创建 bean 第一步先进行代理对象判断，是代理对象则执行完标准后置处理后直接返回，不继续走下面流程
     * @param beanName
     * @param beanDefinition
     * @return
     */
    protected Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {
        // 如果有切面则返回处理过后的代理对象，没有切面处理直接返回 null
        Object bean = applyBeanPostProcessorsBeforeInstantiation(beanDefinition.getBeanClass(), beanName);
        if (bean != null) {
            // 代理对象生成之后，执行 BeanPostProcessor 的后置处理方法（前置处理方法由 InstantiationAwareBeanPostProcessor 进行替换处理了）
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        }
        return bean;
    }

    /**
     * 废弃：处理 aop 的特殊 processor: InstantiationAwareBeanPostProcessor
     * 返回融入切面的代理对象（cglib或jdk）
     * @param beanClass
     * @param beanName
     * @return
     */
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
        // 获取所有 BeanPostProcessor
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            // 找到实现了接口 InstantiationAwareBeanPostProcessor 的 processor
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                // 执行 InstantiationAwareBeanPostProcessor 专门定义的接口 postProcessBeforeInstantiation（处理aop的通知）
                Object result = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessBeforeInstantiation(beanClass, beanName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


    /**
     * 初始化完成注册实现了销毁接口的对象
     *
     * prototype类型的bean每次请求都是一个新创建的，没有向单例bean一样被缓存起来，因此并不会受到容器的管理，也就无需注册销毁方法。
     * 在每次使用完成后由于没有被缓存是会被销毁掉，单例bean不会在使用后被销毁因此需要注册销毁方法在容器关闭前进行统一销毁。
     *
     * @param bean
     * @param beanName
     * @param beanDefinition
     */
    private void registerDisposableBeanIfNecessary(Object bean, String beanName, BeanDefinition beanDefinition) {
        // 增加非单例bean不需要执行销毁方法
        if (beanDefinition.isSingleton()) {
            // 接口 或 xml 两种
            if (bean instanceof DisposableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())) {
                registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
            }
        }
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
        Constructor ctor = null;
        Constructor[] declaredConstructors = beanDefinition.getBeanClass().getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            // 2. 简单比较参数数量即可（忽略类型比较）
            if(args != null && args.length == constructor.getParameterCount()) {
                ctor = constructor;
                break;
            }
        }
        // 3. 根据实例化策略创建对象
        return getInstantiationStrategy().instantiate(beanDefinition, beanName, ctor, args);
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
            Object dealFinishBean = beanPostProcessor.postProcessBeforeInitialization(resultBean, beanName);
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
            Object dealFinishBean = beanPostProcessor.postProcessAfterInitialization(resultBean, beanName);
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

    /**
     * bean实例化完成后的初始化方法执行
     *
     * 两种初始化实现方式：
     *      1、实现 InitializingBean 接口
     *      2、xml 配置中定义了 init-method 属性
     *
     * @param beanName
     * @param bean
     * @param beanDefinition
     */
    protected void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) throws BeansException{
        // 1. 实现了初始化 bean 接口则可以调用 afterPropertiesSet 方法
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }
        // 2. xml 中的 init-method 属性
        String initMethodName = beanDefinition.getInitMethodName();
        if (StrUtil.isNotEmpty(initMethodName)) {
            try {
                // 2.1 反射获取初始化方法
                Method initMethod = beanDefinition.getBeanClass().getMethod(initMethodName);
                // 2.2 反射调用初始化方法
                initMethod.invoke(bean);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new BeansException("找不到xml中定义的 init-method 方法");
            }
        }
    }


    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
