package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/6/10 16:37
 * @Version 1.0
 * @Description
 */
public class A {

    private B b;

    void fun() {
        System.out.println("A.fun 方法执行...........");
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}