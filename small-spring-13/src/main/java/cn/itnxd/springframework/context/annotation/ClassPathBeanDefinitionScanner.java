package cn.itnxd.springframework.context.annotation;

import cn.hutool.core.util.StrUtil;
import cn.itnxd.springframework.beans.annotation.AutowiredAnnotationBeanPostProcessor;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.support.BeanDefinitionRegistry;
import cn.itnxd.springframework.stereotype.Component;

import java.util.Set;

/**
 * @Author niuxudong
 * @Date 2023/6/4 22:28
 * @Version 1.0
 * @Description 继承 ClassPathScanningCandidateComponentProvider 拿到扫描得到的 class 进一步处理
 */
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

        // 增加：想容器注册处理 @Autowired 和 @Value 注解的 BeanPostProcessor
        registry.registerBeanDefinition("cn.itnxd.springframework.context.annotation.internalAutowiredAnnotationProcessor", new BeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
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
