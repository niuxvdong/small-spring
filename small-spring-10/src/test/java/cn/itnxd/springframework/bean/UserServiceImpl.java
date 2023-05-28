package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:32
 * @Version 1.0
 * @Description 这里为实现接口方式的初始化和销毁方法
 */
public class UserServiceImpl implements UserService{

    private String id;
    private String company;
    private String location;

    @Override
    public void getUserInfo() {
        System.out.println("查询用户信息: xxx");
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
}
