package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:32
 * @Version 1.0
 * @Description
 */
public class UserService {

    private String id;

    private UserMapper userMapper;

    public void getUserInfo() {
        String userInfo = userMapper.getUserInfo(this.id);
        System.out.println("查询用户信息: " + userInfo);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserMapper getUserMapper() {
        return userMapper;
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}
