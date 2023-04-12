package cn.itnxd.springframework.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author niuxudong
 * @Date 2023/4/12 22:12
 * @Version 1.0
 * @Description
 */
public class UserMapper {

    private static final Map<String, String> userMap = new HashMap<>();

    static {
        userMap.put("10001", "武松");
        userMap.put("10002", "林冲");
        userMap.put("10003", "吴用");
    }

    public String getUserInfo(String id) {
        return userMap.get(id);
    }
}
