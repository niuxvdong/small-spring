package cn.itnxd.springframework.beans.exception;

/**
 * @Author niuxudong
 * @Date 2023/4/9 19:27
 * @Version 1.0
 * @Description
 */
public class BeansException extends RuntimeException {

    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(String msg, Throwable cause) {
        super(msg, cause);
    }
}