# 第五章 - 实现资源加载器加载 xml 文件注册 Bean

## 一、项目结构

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─itnxd
│  │  │          └─springframework
│  │  │              ├─beans
│  │  │              │  │  PropertyValue.java
│  │  │              │  │  PropertyValues.java
│  │  │              │  │
│  │  │              │  ├─exception
│  │  │              │  │      BeansException.java
│  │  │              │  │
│  │  │              │  └─factory
│  │  │              │      │  BeanFactory.java
│  │  │              │      │
│  │  │              │      ├─config
│  │  │              │      │      BeanDefinition.java
│  │  │              │      │      BeanReference.java
│  │  │              │      │      InstantiationStrategy.java
│  │  │              │      │      SingletonBeanRegistry.java
│  │  │              │      │
│  │  │              │      ├─support
│  │  │              │      │      AbstractAutowireCapableBeanFactory.java
│  │  │              │      │      AbstractBeanDefinitionReader.java
│  │  │              │      │      AbstractBeanFactory.java
│  │  │              │      │      BeanDefinitionReader.java
│  │  │              │      │      BeanDefinitionRegistry.java
│  │  │              │      │      CglibSubclassingInstantiationStrategy.java
│  │  │              │      │      DefaultListableBeanFactory.java
│  │  │              │      │      DefaultSingletonBeanRegistry.java
│  │  │              │      │      SimpleInstantiationStrategy.java
│  │  │              │      │
│  │  │              │      └─xml
│  │  │              │              XmlBeanDefinitionReader.java
│  │  │              │
│  │  │              ├─core
│  │  │              │  └─io
│  │  │              │          ClassPathResource.java
│  │  │              │          DefaultResourceLoader.java
│  │  │              │          FileSystemResource.java
│  │  │              │          Resource.java
│  │  │              │          ResourceLoader.java
│  │  │              │          UrlResource.java
│  │  │              │
│  │  │              └─utils
│  │  └─resources
│  │          hello.txt
│  │          spring.xml
│  │
│  └─test
│      └─java
│          └─cn
│              └─itnxd
│                  └─springframework
│                      │  ApiTest.java
│                      │
│                      └─bean
│                              UserMapper.java
│                              UserService.java

```

## 二、资源加载器实现

有了资源加载器，就可以在xml格式配置文件中声明式地定义bean的信息，资源加载器读取xml文件，解析出bean的信息，然后往容器中注册BeanDefinition。

### 1、顶层资源 Resource 接口

- 任何资源都要实现本接口以便于可以拿到输入流进行操作

```java
public interface Resource {

    /**
     * 获取输入流
     * @return
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;
}
```

### 2、三种资源实现

#### ClassPathResource 类路径资源

```java
public class ClassPathResource implements Resource{

    private final String path;

    private ClassLoader classLoader;

    public ClassPathResource(String path) {
        this(path, null);
    }

    /**
     * 构造器
     *
     * classLoader 如果为空，则获取当前线程类加载器，仍然空则获取本类的类加载器
     *
     * @param path
     * @param classLoader
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        Assert.notNull(path, "path 路径一定不能为空");
        this.path = path;
        this.classLoader = classLoader != null ? classLoader : ClassLoaderUtil.getClassLoader();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // 使用类加载器加载当前路径下的资源
        InputStream inputStream = this.classLoader.getResourceAsStream(this.path);
        if (inputStream == null) {
            throw new FileNotFoundException("无法打开，路径【" + this.path + "】不存在！");
        }
        return inputStream;
    }
}
```

#### FileSystemResource 本地文件资源

```java
public class FileSystemResource implements Resource{

    private final File file;

    private final String path;

    public FileSystemResource(File file) {
        this.file = file;
        this.path = file.getPath();
    }

    public FileSystemResource(String path) {
        this.file = new File(path);
        this.path = path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.file.toPath());
    }
}
```

#### UrlResource 网络 HTTP 资源

```java
public class UrlResource implements Resource{

    private final URL url;

    public UrlResource(URL url) {
        Assert.notNull(url,"URL 一定不能为空");
        this.url = url;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // 1. 创建 url 的连接
        URLConnection urlConnection = this.url.openConnection();
        try {
            // 2. 简化判断连接是否成功以及响应码以及请求方法请求头设置
            return urlConnection.getInputStream();
        } catch (IOException e) {
            // 3. 发生异常则关闭连接
            if (urlConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlConnection).disconnect();
            }
            throw e;
        }
    }
}
```

### 3、顶层资源加载器 ResourceLoader 接口

- 用于获取资源，只有一个获取资源接口，可以从 类路径、本地文件系统、url http 资源获取。
- 获取到资源，可以通过 Resource 接口获取 InputStream 进行具体操作

```java
public interface ResourceLoader {

    String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * 资源加载器接口：获取资源（classpath/url/fileSystem）
     * @param location
     * @return
     */
    Resource getResource(String location);
}
```

### 4、ResourceLoader 实现类 DefaultResourceLoader

```java
public class DefaultResourceLoader implements ResourceLoader{

    /**
     * 实现获取资源的方法
     * @param location
     * @return
     */
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location 一定不能为空");
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            // 类路径流
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
        } else {
            try {
                // url流
                URL url = new URL(location);
                return new UrlResource(url);
            } catch (MalformedURLException e) {
                // 本地文件流
                return new FileSystemResource(location);
            }
        }
    }
}
```

### 5、顶层 BeanDefinition 读取器接口 BeanDefinitionReader

- 获取BeanDefinitionRegistry注册中心（用来注册BeanDefinition） 
- 获取资源加载器ResourceLoader（得到资源Resource） 
- 装载 BeanDefinition 信息（1、2为3服务）
  - 解析 xml 文件得到 BeanDefinition 信息
  - 注册 BeanDefinition 信息到 DefaultListableBeanFactory 的 beanDefinitionMap

```java
public interface BeanDefinitionReader {

    /**
     * 获取BeanDefinitionRegistry注册中心（用来注册BeanDefinition）
     * @return
     */
    BeanDefinitionRegistry getRegistry();

    /**
     * 获取资源加载器ResourceLoader（得到资源Resource）
     * @return
     */
    ResourceLoader getResourceLoader();

    /**
     * 装载 BeanDefinition 信息（通过资源加载器得到资源，解析后得到BeanDefinition，通过注册中心进行注册）
     * @param resource
     * @throws BeansException
     */
    void loadBeanDefinitions(Resource resource) throws BeansException;

    void loadBeanDefinitions(String location) throws BeansException;

    void loadBeanDefinitions(String[] locations) throws BeansException;
}
```

### 6、BeanDefinition 抽象实现类 AbstractBeanDefinitionReader

- 本类持有 BeanDefinitionRegistry 用来注册 BeanDefinition 信息、ResourceLoader 获取资源加载器用来读取到资源 Resource
- 二者通过构造方法注入
- BeanDefinitionReader需要有获取资源的能力，且读取bean定义信息后需要往容器中注册BeanDefinition，因此BeanDefinitionReader的抽象实现类AbstractBeanDefinitionReader拥有ResourceLoader和BeanDefinitionRegistry两个属性。


```java
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader{

    private BeanDefinitionRegistry registry;

    private ResourceLoader resourceLoader;

    /**
     * 单参构造
     * @param registry
     */
    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this(registry, new DefaultResourceLoader());
    }

    /**
     * 两参构造
     * @param registry
     * @param resourceLoader
     */
    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        this.registry = registry;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 实现父接口BeanDefinitionReader的获取注册中心方法
     * @return
     */
    @Override
    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }

    /**
     * 实现父接口BeanDefinitionReader的获取资源加载器方法
     * @return
     */
    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
```

### 7、BeanDefinitionReader 的 XML 具体实现子类 XmlBeanDefinitionReader

- 继承抽象 AbstractBeanDefinitionReader
- 通过构造方法注入 **BeanDefinitionRegistry** 和 ResourceLoader（不传递默认为 DefaultResourceLoader）
  - 在使用时，一般 BeanDefinitionRegistry 会传入 BeanFactory，即 DefaultListableBeanFactory，这个 BeanFactory 实现了 BeanDefinitionRegistry 接口。
- 实现 BeanDefinitionReader 的三个 loadBeanDefinitions 方法
- 其他两种都会调用 `loadBeanDefinitions(Resource resource)` 进行处理
- 核心方法为 `doLoadBeanDefinitions` 即从 xml 配置文件进行解析，采用 hutool 工具类的 dom 工具进行解析

**核心逻辑如下：**

```
beanDefinition.getPropertyValues().addPropertyValue(pv);
getRegistry().registerBeanDefinition(beanName, beanDefinition);
```

**具体实现：**

```java
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
```

## 三、简单测试

### xml 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>

    <bean id="userMapper" class="cn.itnxd.springframework.bean.UserMapper"/>

    <bean id="userService" class="cn.itnxd.springframework.bean.UserService">
        <property name="id" value="10001"/>
        <property name="userMapper" ref="userMapper"/>
    </bean>

</beans>
```

### 具体测试

```java
public class ApiTest {

    @Test
    public void test_resource() throws IOException {
        // classpath 文件
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:hello.txt");
        String res = IoUtil.readUtf8(resource.getInputStream());
        System.out.println(res);

        // url 流
        Resource urlResource = resourceLoader.getResource("https://www.baidu.com");
        String urlRes = IoUtil.readUtf8(urlResource.getInputStream());
        System.out.println(urlRes);

        // 本地文件流
        Resource fileResource = resourceLoader.getResource("src/main/resources/hello.txt");
        String fileRes = IoUtil.readUtf8(fileResource.getInputStream());
        System.out.println(fileRes);
    }

    @Test
    public void test_BeanFactory() {
        // 1. 初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 解析xml
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions("classpath:spring.xml");

        // 3. 获取bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.getUserInfo();
    }
}
```
