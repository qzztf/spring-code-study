# Spring AOP 用法二

在上一篇中讲到通过xml配置`ProxyFactoryBean`来创建代理对象，在这一篇中看看通过编程的方式配置`ProxyFactory`。该类继承自`ProxyCreatorSupport`（之前提到过该类提供了一些创建代理对象的基础方法），提供了配置目标对象，代理接口和拦截器的功能。

## 基本用法

```java
DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
xmlBeanDefinitionReader.loadBeanDefinitions("AopXmlSimpleConfig.xml");
// 获取需求代理的目标对象
IBusinessService bean = beanFactory.getBean("businessService", IBusinessService.class);
// 获取需要增强的通知
Advice userBeforeAdvice = beanFactory.getBean("userBeforeAdvice", Advice.class);
// 将目标对象传入构造函数，创建代理工厂
ProxyFactory proxyFactory = new ProxyFactory(bean);
//添加我们的通知
proxyFactory.addAdvice(userBeforeAdvice);
// 最终获取到代理对象
IBusinessService proxy = (IBusinessService) proxyFactory.getProxy();
//调用代理对象的方法
proxy.sayAgain();
```

通过以上代码，我们创建出了最终的代理对象。大致步骤如下：

1. 通过构造函数，传入目标对象，创建代理工厂对象
2. 添加通知
3. 获取代理对象
4. 调用代理对象的方法

执行结果：

```
Before method advice
again
```

打印结果也说明了我们的代理对象正常工作，先执行了前置通知，再调用了目标对象的方法。

最重要的获取代理对象的方法都是在父类中完成的，与`ProxyFactoryBean`类似，该类只是提供了另外一种配置的方式。

