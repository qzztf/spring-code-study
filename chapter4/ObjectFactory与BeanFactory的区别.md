# ObjectFactory 与BeanFactory 的区别

Spring 体系中有一些跟 Bean获取相关的接口，比如：`ObjectFactory`、`BeanFactory `、`FactoryBean`、`ObjectProvider`等。有时候会让人不知道该使用哪一个。下面具体看一下区别。

##  BeanFactory

该接口是整个Spring容器的顶层接口，定义了从容器中获取Bean的方法。当我们的应用需要从容器中获取Bean的时候用此接口。在这里我们可以将整个容器看成是一个黑盒，不需要关心它是如何获取的，我们只关心结果。

## ObjectFactory

此接口定义了一个工厂，可以在调用时返回一个对象实例(可能是共享的或独立的)。

此接口通常用于封装一个泛型工厂，该工厂在每次调用时返回某个目标对象的新实例(原型)。

这个接口类似于`FactoryBean`，但是后者的实现类通常被定义为`BeanFactory`中的SPI实例，而该类的实现通常被作为API(通过注入)提供给其他bean。因此，getObject()方法具有不同的异常处理行为

## FactoryBean



## ObjectProvider