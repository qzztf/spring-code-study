# Spring AOP 用法

前面讲到了AOP相关的思想和基本概念，实现方式可分为静态编绎型和代理两种方式。前者的代表作是`AspectJ`，在编绎阶段将通知织入到class中，需要用到特殊的工具来编绎。`AspectJ`定义了一种表达式语言来定义连接点，Spring 默认是基于JDK动态代理来实现AOP，并且只支持方法作为切入点。

## 如何使用

使用Spring AOP有以下几种方式：

1. 配置`ProxyFactoryBean`，显式地设置advisors, advice, target等