package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/6/10 16:37
 * @Version 1.0
 * @Description
 */
public class A {

    private B b;

    private long endTime;

    public void initTime() {
        endTime = System.currentTimeMillis();
    }

    public void fun() {
        System.out.println("A.fun 方法执行...........");
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
