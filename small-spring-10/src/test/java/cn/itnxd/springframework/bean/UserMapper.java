package cn.itnxd.springframework.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/12 22:12
 * @Version 1.0
 * @Description 这里为配置化的初始化和销毁方法
 */
public class UserMapper {

    private static final Map<String, String> userMap = new HashMap<>();

    static {

    }

    public String getUserInfo(String id) {
        return userMap.get(id);
    }

    // 增加xml配置类的初始化方法和销毁方法

    /**
     * 这里可由做一些初始化操作，例如将我们之前静态代码块里的内容搬到这里即可
     */
    public void myInitMethod() {
        userMap.put("10001", "武松");
        userMap.put("10002", "林冲");
        userMap.put("10003", "吴用");

        System.out.println("执行 userMapper 的 myInitMethod 方法");
    }

    /**
     * 容器或虚拟机关闭时 做一个 map 的清空操作
     */
    public void myDestroyMethod() {
        userMap.clear();

        System.out.println("执行 userMapper 的 myDestroyMethod 方法");
    }
}
