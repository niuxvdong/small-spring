package cn.itnxd.springframework.aop.framework;

import cn.itnxd.springframework.aop.AdvisedSupport;

/**
 * @Author niuxudong
 * @Date 2023/6/3 10:34
 * @Version 1.0
 * @Description 添加代理工厂，解决 jdk 代理和 cglib 代理选择问题
 */
public class ProxyFactory {

    private AdvisedSupport advisedSupport;

    public ProxyFactory(AdvisedSupport advisedSupport) {
        this.advisedSupport = advisedSupport;
    }

    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private AopProxy createAopProxy() {
        if (advisedSupport.isProxyTargetClass()) {
            return new CglibAopProxy(advisedSupport);
        }
        return new JdkDynamicAopProxy(advisedSupport);
    }
}
