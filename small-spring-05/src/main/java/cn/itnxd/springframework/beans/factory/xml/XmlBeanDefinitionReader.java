package cn.itnxd.springframework.beans.factory.xml;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.itnxd.springframework.beans.PropertyValue;
import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.config.BeanDefinition;
import cn.itnxd.springframework.beans.factory.config.BeanReference;
import cn.itnxd.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import cn.itnxd.springframework.beans.factory.support.BeanDefinitionRegistry;
import cn.itnxd.springframework.core.io.Resource;
import cn.itnxd.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author niuxudong
 * @Date 2023/4/13 22:22
 * @Version 1.0
 * @Description
 *
 * BeanDefinitionReader是读取bean定义信息的抽象接口，XmlBeanDefinitionReader是从xml文件中读取的实现类。
 * BeanDefinitionReader需要有获取资源的能力，且读取bean定义信息后需要往容器中注册BeanDefinition，
 * 因此BeanDefinitionReader的抽象实现类AbstractBeanDefinitionReader拥有ResourceLoader和BeanDefinitionRegistry两个属性。
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    /**
     * 单参构造
     * @param registry
     */
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * 两参构造
     * @param registry
     * @param resourceLoader
     */
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        super(registry, resourceLoader);
    }

    /**
     * 实现父接口的location数组加载Bean的方法，即遍历进行调用即可
     * @param locations
     * @throws BeansException
     */
    @Override
    public void loadBeanDefinitions(String[] locations) throws BeansException {
        for (String location : locations) {
            loadBeanDefinitions(location);
        }
    }

    /**
     * 实现location本地资源或文件系统的BeanDefinition信息装载
     * 将location资源转化为Resource资源调用 public void loadBeanDefinitions(Resource resource) 方法即可
     * @param location
     * @throws BeansException
     */
    @Override
    public void loadBeanDefinitions(String location) throws BeansException {
        // 获取到资源加载器 用来根据传入的字符串获取资源（url,classpath,file）
        Resource resource = getResourceLoader().getResource(location);
        loadBeanDefinitions(resource);
    }

    /**
     * 实现Resource资源的BeanDefinition信息装载
     *
     * 其他两种参数的方法重载最终都会走到这里的核心逻辑。
     *
     * @param resource
     * @throws BeansException
     */
    @Override
    public void loadBeanDefinitions(Resource resource) throws BeansException {
        try {
            // 1. 获取到资源的输入流
            try(InputStream is = resource.getInputStream()) {
                // 2. 核心处理逻辑
                doLoadBeanDefinitions(is);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new BeansException("从资源【" + resource + "】解析xml文档异常, e：", e);
        }
    }

    /**
     * 从资源解析并加载BeanDefinition注册到容器核心处理逻辑
     *
     * @param is
     */
    private void doLoadBeanDefinitions(InputStream is) throws ClassNotFoundException {
        // 1. 从输入流读取document信息并获取根信息
        Document document = XmlUtil.readXML(is);
        Element root = document.getDocumentElement();
        // 2. 获取子节点列表
        NodeList childNodes = root.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i ++) {
            // 3. 不是Element对象则continue
            if (!(childNodes.item(i) instanceof Element)) continue;
            // 4. 不是Bean标签则continue
            if (!("bean".equals(childNodes.item(i).getNodeName()))) continue;

            // 5. 解析bean标签 得到id、class、name属性
            Element bean = (Element) childNodes.item(i);
            String id = bean.getAttribute("id");
            String name = bean.getAttribute("name");
            String className = bean.getAttribute("class");

            // 6. 获取bean定义的class
            Class<?> clazz = Class.forName(className);

            // 7. 获取 beanName 信息，优先获取id，空则读取 name，还空则读取类名首字母小写
            String beanName = StrUtil.isNotEmpty(id) ? id : name;
            if (StrUtil.isEmpty(beanName)) {
                // getSimpleName 获取的是 a.b.c 中的 c
                beanName = StrUtil.lowerFirst(clazz.getSimpleName());
            }

            // 8. 创建 BeanDefinition 对象
            BeanDefinition beanDefinition = new BeanDefinition(clazz);

            // 9. 解析bean标签的子标签填充BeanDefinition的属性信息
            for (int j = 0; j < bean.getChildNodes().getLength(); j ++) {
                // 10. 不是Element对象则continue，不是property属性标签也continue
                if (!(bean.getChildNodes().item(j) instanceof Element)) continue;
                if (!("property".equals(bean.getChildNodes().item(j).getNodeName()))) continue;

                // 11. 解析 property 标签，得到 name value ref 属性信息
                Element property = (Element) bean.getChildNodes().item(j);
                String propertyName = property.getAttribute("name");
                String propertyValue = property.getAttribute("value");
                String propertyRef = property.getAttribute("ref");

                // 12. 如果有引用类型即属性为bean，则创建BeanReference标记
                Object value = StrUtil.isNotEmpty(propertyRef) ? new BeanReference(propertyRef) : propertyValue;

                // 13. 为BeanDefinition填充属性
                PropertyValue pv = new PropertyValue(propertyName, value);
                beanDefinition.getPropertyValues().addPropertyValue(pv);
            }
            // 14. BeanDefinition 已经准备好，需要注入到容器中
            getRegistry().registerBeanDefinition(beanName, beanDefinition);
        }
    }
}
