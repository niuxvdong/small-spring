package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:32
 * @Version 1.0
 * @Description
 */
public class UserService {

    private String name;

    public UserService(String name) {
        this.name = name;
    }

    public void getUserInfo() {
        System.out.println("查询用户信息: " + this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserService{" +
                "name='" + name + '\'' +
                '}';
    }
}
