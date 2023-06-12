package cn.itnxd.springframework.bean;

/**
 * @Author niuxudong
 * @Date 2023/6/10 16:37
 * @Version 1.0
 * @Description
 */
public class B {

    private A a;

    private long endTime;

    public void initTime() {
        endTime = System.currentTimeMillis();
    }
    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
