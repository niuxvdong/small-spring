package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:32
 * @Version 1.0
 * @Description
 */
public class UserService {

    private String id;

    // 增加属性用于测试PostProcessor
    private String company;
    private String location;

    private UserMapper userMapper;

    public void getUserInfo() {
        String userInfo = userMapper.getUserInfo(this.id);
        System.out.println("查询用户信息: " + userInfo);
    }

    //=================================================================//

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "UserService{" +
                "id='" + id + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", userMapper=" + userMapper +
                '}';
    }
}
