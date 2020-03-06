# Spring AOP 用法二

在上一篇中讲到通过xml配置`ProxyFactoryBean`来创建代理对象，在这一篇中看看通过编程的方式配置`ProxyFactory`。该类继承自`ProxyCreatorSupport`（之前提到过该类提供了一些创建代理对象的基础方法），提供了配置目标对象，代理接口和拦截器的功能。