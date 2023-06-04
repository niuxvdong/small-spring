package cn.itnxd.springframework.bean;

import cn.itnxd.springframework.stereotype.Component;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:32
 * @Version 1.0
 * @Description 这里为实现接口方式的初始化和销毁方法
 */
@Component("userService")
public class UserServiceImpl implements UserService{

    private String id;
    private String company;
    private String location;

    private String username;

    @Override
    public void getUserInfo() {
        System.out.println("查询用户信息: " + username);
    }

    //=================================================================//

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
