package cn.itnxd.springframework.bean;

import cn.itnxd.springframework.beans.exception.BeansException;
import cn.itnxd.springframework.beans.factory.DisposableBean;
import cn.itnxd.springframework.beans.factory.InitializingBean;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:32
 * @Version 1.0
 * @Description 这里为实现接口方式的初始化和销毁方法
 */
public class UserService implements InitializingBean, DisposableBean {

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

    /**
     * 实现销毁 bean 接口的收尾方法
     * @throws BeansException
     */
    @Override
    public void destroy() throws BeansException {
        System.out.println("执行 userService 的 DisposableBean.destroy");
    }

    /**
     * 实现初始化 bean 接口方法
     * @throws BeansException
     */
    @Override
    public void afterPropertiesSet() throws BeansException {
        System.out.println("执行 userService 的 InitializingBean.afterPropertiesSet");
    }
}
