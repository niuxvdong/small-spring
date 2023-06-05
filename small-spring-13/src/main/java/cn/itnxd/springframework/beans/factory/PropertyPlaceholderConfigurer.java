package cn.itnxd.springframework.beans.factory;

import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.PropertyValues;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanFactoryPostProcessor;
import cn.itnxd.springframework.core.io.DefaultResourceLoader;
import cn.itnxd.springframework.core.io.Resource;
import cn.itnxd.springframework.utils.StringValueResolver;

import java.io.IOException;
import java.util.Properties;

/**
 * @Author niuxudong
 * @Date 2023/6/4 21:40
 * @Version 1.0
 * @Description 处理属性占位符
 */
public class PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {

    // 占位符前缀
    public static final String PLACEHOLDER_PREFIX = "${";
    // 占位符后缀
    public static final String PLACEHOLDER_SUFFIX = "}";

    // yml 或 properties 配置文件路径
    private String location;

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

    private Properties loadProperties() {
        try {
            // 通过资源加载器加载资源
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource(location);
            // 使用 Properties 工具包加载配置信息
            Properties properties = new Properties();
            properties.load(resource.getInputStream());
            return properties;
        } catch (IOException e) {
            throw new BeansException("不能加载 properties 配置文件信息", e);
        }
    }

    /**
     * 解析占位符拿到真实值返回
     * @param strVal
     * @param properties
     * @return
     */
    private String resolvePlaceholder(String strVal, Properties properties) {
        StringBuffer buf = new StringBuffer(strVal);
        int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
        int endIndex = strVal.indexOf(PLACEHOLDER_SUFFIX);
        // ${} 合法
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            // key 去掉 ${} 得到属性 key
            String propKey = strVal.substring(startIndex + 2, endIndex);
            // 从properties配置文件中得到对应的 属性 value
            String propVal = properties.getProperty(propKey);
            // 将 ${xxx} 替换为 对应的配置文件中的值
            buf.replace(startIndex, endIndex + 1, propVal);
        }
        return buf.toString();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
