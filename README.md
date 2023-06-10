# 手写 Spring 框架学习

- 参考项目一：[https://github.com/fuzhengwei/small-spring](https://github.com/fuzhengwei/small-spring)
- 参考项目二：[https://github.com/DerekYRC/mini-spring](https://github.com/DerekYRC/mini-spring)

# 笔记记录

## IOC 篇

1. [第一章 - 实现最简单的 Bean 容器](https://github.com/niuxvdong/small-spring/blob/master/small-spring-01/README.md)
2. [第二章 - 实现 Bean 的定义、注册、获取](https://github.com/niuxvdong/small-spring/blob/master/small-spring-02/README.md)
3. [第三章 - 实现 Bean 的两种实例化策略（JDK & Cglib）](https://github.com/niuxvdong/small-spring/blob/master/small-spring-03/README.md)
4. [第四章 - Bean 实例化完成增加基本属性注入及 Bean 注入](https://github.com/niuxvdong/small-spring/blob/master/small-spring-04/README.md)
5. [第五章 - 实现资源加载器加载 xml 文件注册 Bean](https://github.com/niuxvdong/small-spring/blob/master/small-spring-05/README.md)
6. [第六章 - 应用上下文 ApplicationContext 实现](https://github.com/niuxvdong/small-spring/blob/master/small-spring-06/README.md)
7. [第七章 - 增加初始化方法和销毁方法](https://github.com/niuxvdong/small-spring/blob/master/small-spring-07/README.md)
8. [第八章 - 实现 Aware 接口使子类可以感知到容器对象（一）](https://github.com/niuxvdong/small-spring/blob/master/small-spring-08/README-1.md)
9. [第八章 - 实现 Bean 作用域以及 FactoryBean （二）](https://github.com/niuxvdong/small-spring/blob/master/small-spring-08/README-2.md)
10. [第九章 - 容器事件和事件监听器](https://github.com/niuxvdong/small-spring/blob/master/small-spring-09/README.md)
11. [第十四章 - 解决代理对象生成后没有继续向下执行的 bug](https://github.com/niuxvdong/small-spring/blob/master/small-spring-14/README.md)

## AOP 篇

12. [第十章 - 实现切点表达式以及基于 JDK 和 Cglib 实现 AOP 切面](https://github.com/niuxvdong/small-spring/blob/master/small-spring-10/README.md)
13. [第十一章 - 把 AOP 切面逻辑融入 Bean 生命周期](https://github.com/niuxvdong/small-spring/blob/master/small-spring-11/README.md)

## 拓展篇

14. [第十二章 - 支持包扫描和 ${} 占位符配置解析](https://github.com/niuxvdong/small-spring/blob/master/small-spring-12/README.md)
15. [第十三章 - 增加 @Value 和 @Autowired 属性自动注入](https://github.com/niuxvdong/small-spring/blob/master/small-spring-13/README.md)
16. [第十五章 - 添加数据类型转换器并融入 Bean 生命周期](https://github.com/niuxvdong/small-spring/blob/master/small-spring-15/README.md)
17. [第十六章 - 通过三级缓存解决循环依赖问题](https://github.com/niuxvdong/small-spring/blob/master/small-spring-16/README.md)