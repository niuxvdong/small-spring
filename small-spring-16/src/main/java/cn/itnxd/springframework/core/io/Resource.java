package cn.itnxd.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author niuxudong
 * @Date 2023/4/12 22:52
 * @Version 1.0
 * @Description 顶层资源接口
 */
public interface Resource {

    /**
     * 获取输入流
     * @return
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;
}
