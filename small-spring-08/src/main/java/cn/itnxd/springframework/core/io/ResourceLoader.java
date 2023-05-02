package cn.itnxd.springframework.core.io;

/**
 * @Author niuxudong
 * @Date 2023/4/12 23:27
 * @Version 1.0
 * @Description 资源加载器顶层接口
 */
public interface ResourceLoader {

    String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * 资源加载器接口：获取资源（classpath/url/fileSystem）
     * @param location
     * @return
     */
    Resource getResource(String location);
}
