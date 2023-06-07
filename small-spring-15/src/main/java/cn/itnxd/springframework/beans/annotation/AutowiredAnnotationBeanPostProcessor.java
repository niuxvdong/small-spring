package cn.itnxd.springframework.beans.annotation;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.itnxd.springframework.beans.PropertyValues;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.BeanFactory;
import cn.itnxd.springframework.beans.factory.BeanFactoryAware;
import cn.itnxd.springframework.beans.factory.ConfigurableListableBeanFactory;
import cn.itnxd.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Field;

/**
 * @Author niuxudong
 * @Date 2023/6/5 22:15
 * @Version 1.0
 * @Description 继承自 InstantiationAwareBeanPostProcessor 特殊的 BeanPostProcessor（之前用它来处理 aop 生成的代理对象）
 *  这里用它来处理 两个注解的解析。在包扫描器 ClassPathBeanDefinitionScanner 中手动注入容器。
 */
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
