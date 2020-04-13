前面讲到了使用XML 配置`ProxyFactoryBean`，结合注解配置`@Bean`也可以实现代码配置。还讲到了代码配置`ProxyFactory`。前者与Spring IOC 容器结合的更紧密，不需要自己调用`getProxy()`方法，从IOC 容器中获取的Bean 已经是最终的代理对象。

这两种方式需要我们自己设置目标对象，通知以及代理接口，使用起来还是比较繁琐的。每当我们需要一个代理对象时，就需要配置`ProxyFactoryBean`或`ProxyFactory`。

有没有更好的方式，只需要做简单的配置就可以为多个对象生成代理对象呢？

我们已经有了创建代理的方式，就是前面介绍的工厂类，剩下的工作只需要解决如何配置和如何解析配置的问题就可以了。

下面介绍最核心的类。

## AbstractAutoProxyCreator

看看Spring 官方介绍：

> 该抽象类实现了BeanPostProcessor接口，用AOP代理来包装每个合适的bean，并在调用bean本身之前委托给指定的拦截器。这个类区分了“公共”拦截器和“特定”拦截器，前者用于它创建的所有代理，后者用于每个bean实例。可以不需要任何通用的拦截器。如果有，则可以使用`interceptorNames`属性设置它们。与org.springframework.aop.framework.ProxyFactoryBean 一样，使用拦截器名称而不是bean引用来正确处理原型顾问和拦截器：例如，支持有状态的混合。`interceptorNames`属性支持任何通知类型。
>
> 如果有大量的bean需要用类似的代理(即委托给相同的拦截器)来包装，那么这种自动代理特别有用。可以在bean工厂中注册一个这样的后处理程序，而不是为x个目标bean进行x个重复的代理定义，来达到相同的效果。
>
> 子类可以应用任何策略来决定一个bean是否被代理，例如通过类型、名称、bean定义细节等。它们还可以返回额外的拦截器，这些拦截器应该只应用于特定的bean实例。`BeanNameAutoProxyCreator`是一个简单的实现类，它通过指定名称识别要代理的bean。
>
> 可以使用任意数量的`TargetSourceCreator`实现来创建自定义目标源:例如，来共享原型对象。只要`TargetSourceCreator`指定了自定义`TargetSource`，即使没有通知，也会发生自动代理。如果没有设置TargetSourceCreator，或者没有匹配上，那么默认情况下将使用 `SingletonTargetSource`来包装目标bean实例.

从上面的描述中，可以看出此类实现了`BeanPostProcessor`接口，拦截bean的创建过程。并提供了自定义获取目标对象的方式，以及识别通知，创建代理对象的核心逻辑。子类提供了多种更具体的创建代理的策略。

### 类图

![AbstractAutoProxyCreator](./AbstractAutoProxyCreator.png)

从上图中可以看到此类实现了`SmartInstantiationAwareBeanPostProcessor`接口，该接口在前面Bean的初始化中讲到过，如果`postProcessBeforeInstantiation`方法返回了非`null`对象，则将会打断原bean的初始化过程，从而使用该方法返回的对象。