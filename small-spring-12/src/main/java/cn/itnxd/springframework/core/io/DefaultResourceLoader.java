package cn.itnxd.springframework.core.io;

import cn.hutool.core.lang.Assert;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @Author niuxudong
 * @Date 2023/4/12 23:32
 * @Version 1.0
 * @Description 资源加载器接口实现类
 */
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
