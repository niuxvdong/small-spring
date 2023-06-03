package cn.itnxd.springframework.core.io;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassLoaderUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author niuxudong
 * @Date 2023/4/12 22:54
 * @Version 1.0
 * @Description 类路径流操作
 */
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
