package cn.itnxd.springframework.core.io;

import cn.hutool.core.lang.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @Author niuxudong
 * @Date 2023/4/12 22:54
 * @Version 1.0
 * @Description  HTTP 的方式读取云文件转换为输入流
 */
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
