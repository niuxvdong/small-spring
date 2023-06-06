package cn.itnxd.springframework.beans.factory.support;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.ConfigurableBeanFactory;
import cn.itnxd.springframework.beans.factory.FactoryBean;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;
import cn.itnxd.springframework.utils.StringValueResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:38
 * @Version 1.0
 * @Description 抽象Bean工厂，继承单例实现（具有单例注册能力），实现顶层Bean工厂接口（修改：实现ConfigurableBeanFactory）
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    // 增加：持有 beanPostProcessors
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private final Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    // 增加存储 value 解析器的集合
    private final List<StringValueResolver> embeddedValueResolvers = new ArrayList<>();

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
            // 1.1 获取到的bean非空则需要判断是否实现了工厂bean接口（包装一个方法进行实现）
            return getObjectForBeanInstance(bean, beanName);
        }
        // 2. 单例 Bean 不存在则创建 Bean
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        // 3. 根据 beanDefinition 创建 Bean
        bean = createBean(beanName, beanDefinition, args);
        // 3.1 bean 为空且创建完成后，同样需要判断是否实现了工厂bean接口
        return getObjectForBeanInstance(bean, beanName);
    }

    private <T> T getObjectForBeanInstance(Object bean, String beanName) {
        Object obj = bean;
        if (bean instanceof FactoryBean) {
            FactoryBean<?> factoryBean = (FactoryBean<?>) bean;
            // 1. 工厂bean是单例，则从缓存map中取出
            try {
                if (factoryBean.isSingleton()) {
                    obj = this.factoryBeanObjectCache.get(beanName);
                    if (obj == null) {
                        // 2. 缓存中没有，则调用getObject来获取并存到缓存中
                        obj = factoryBean.getObject();
                        this.factoryBeanObjectCache.put(beanName, obj);
                    }
                } else {
                    // 3. 工厂bean是prototype，则无需添加到缓存
                    obj = factoryBean.getObject();
                }
            } catch (Exception e) {
                throw new BeansException("工厂bean抛出异常，e: {}", e);
            }
        }
        return (T) obj;
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

    /**
     * 添加解析器到 embeddedValueResolvers 保存
     *
     * @param valueResolver
     */
    public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
        this.embeddedValueResolvers.add(valueResolver);
    }

    /**
     * 对传入的字符串调用注册进来的解析器进行解析，返回解析结果
     *
     * @param value
     * @return
     */
    public String resolveEmbeddedValue(String value) {
        String result = value;
        for (StringValueResolver resolver : this.embeddedValueResolvers) {
            result = resolver.resolveStringValue(result);
        }
        return result;
    }
}
