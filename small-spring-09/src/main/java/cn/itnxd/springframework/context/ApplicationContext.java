package cn.itnxd.springframework.context;

import cn.itnxd.springframework.beans.factory.HierarchicalBeanFactory;
import cn.itnxd.springframework.beans.factory.ListableBeanFactory;
import cn.itnxd.springframework.core.io.ResourceLoader;

/**
 * @Author niuxudong
 * @Date 2023/4/17 22:57
 * @Version 1.0
 * @Description ApplicationContext除了拥有BeanFactory的所有功能外，还支持特殊类型bean，如：BeanFactoryPostProcessor
 *              和BeanPostProcessor的自动识别、资源加载、容器事件和监听器、国际化支持、单例bean自动初始化等。
 *              BeanFactory是spring的基础设施，面向spring本身；而ApplicationContext面向spring的使用者，
 *
 *              核心流程逻辑在：AbstractApplicationContext#refresh方法。
 *
 *              本接口为顶层核心接口，暂不定义方法，具有父接口的获取bean的方法，包括根据类型获取；以及获取分类BeanFactory；
 *              以及资源加载器得到资源Resource
 */
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, ResourceLoader {
}
