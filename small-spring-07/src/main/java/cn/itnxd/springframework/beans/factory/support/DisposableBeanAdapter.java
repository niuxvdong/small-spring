package cn.itnxd.springframework.beans.factory.support;

import cn.hutool.core.util.StrUtil;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.DisposableBean;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author niuxudong
 * @Date 2023/5/2 10:04
 * @Version 1.0
 * @Description DisposableBean 接口的销毁方法有两种方式：
 *      1、实现了 DisposableBean 接口
 *      2、xml 中定义了 destroy-method 属性
 *
 *   与 initializingBean 接口不同的是，初始化方法可以有多个，因此在执行初始化方法时直接依次执行
 *   所有 init 方法即可，包括 xml 中的以及 实现了接口方式的。
 *   而 销毁方法则不同，销毁只需要执行一次即可，因此，需要做一下特殊处理，由于我们并不需要关注要
 *   执行哪些销毁方法，因此，可以定义个适配器 adapter 来适配两种类型的销毁方法。
 */
public class DisposableBeanAdapter implements DisposableBean {

    private final Object bean;
    private final String beanName;
    private final String destroyMethodName;

    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
        this.bean = bean;
        this.beanName = beanName;
        this.destroyMethodName = beanDefinition.getDestroyMethodName();
    }

    /**
     * 适配器重写方法实现两种无需关注细节的适配
     *
     * @throws BeansException
     */
    @Override
    public void destroy() throws BeansException {
        // 1. 实现接口 DisposableBean 的优先
        if (bean instanceof DisposableBean) {
            ((DisposableBean) bean).destroy();
        }
        // 2. xml 配置的 destroy-method 方法
        // （没有实现接口时执行这里 或者 实现了接口但是 销毁方法不是 destroy 时执行）
        // 这里其实保证了接口中定义的 destroy 方法只执行一次
        if (StrUtil.isNotEmpty(destroyMethodName) && !(bean instanceof DisposableBean && "destroy".equals(destroyMethodName))) {
            try {
                Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
                destroyMethod.invoke(bean);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new BeansException("找不到xml中定义的 destroy-method 方法");
            }
        }
    }
}
