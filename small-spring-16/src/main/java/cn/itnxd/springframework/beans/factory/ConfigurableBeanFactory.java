package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.factory.config.BeanPostProcessor;
import cn.itnxd.springframework.beans.factory.config.SingletonBeanRegistry;
import cn.itnxd.springframework.core.convert.ConversionService;
import cn.itnxd.springframework.utils.StringValueResolver;

/**
 * @Author niuxudong
 * @Date 2023/4/17 23:22
 * @Version 1.0
 * @Description 接口的多继承
 *
 * 配置化BeanFactory继承HierarchicalBeanFactory拥有了可以获取父类BeanFactory的能力
 *                  继承SingletonBeanRegistry拥有了获取单例bean的能力
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    /**
     * 拥有添加BeanPostProcessor的方法
     *
     * @param beanPostProcessor
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    /**
     * 添加：销毁单例bean接口
     */
    void destroySingletons();

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

    /**
     * 增加注册类型转换器 ConversionService 方法
     * @param conversionService
     */
    void setConversionService(ConversionService conversionService);

    /**
     * 增加获取类型转换器 ConversionService 方法
     * @return
     */
    ConversionService getConversionService();
}
