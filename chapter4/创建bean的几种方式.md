# Spring 创建Bean的方式

在Spring创建Bean时，会遵循下面的顺序来创建。这个顺序是在`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])`方法的内在逻辑。

1. `InstantiationAwareBeanPostProcessor`
2. `Instance Supplier`
3. factory method
4. constructor autowiring
5. simple instantiation

##  InstantiationAwareBeanPostProcessor

`InstantiationAwareBeanPostProcessor`接口在实例化Bean之前和实例化Bean之后设置属性或者自动装配之前调用。通常用于阴止特定殊目标bean的默认实例化行为，例如创建具有特定目标对象的代理(池目标、延迟初始化目标等)，或者实现额外的注入策略，如字段注入。
**注意**: 此接口为专用接口，主要供框架内部使用。建议尽可能实现简单的`BeanPostProcessor`接口，或者继承`InstantiationAwareBeanPostProcessorAdapter`，以避免扩展这个接口导致误操作。

在Spring中有

1. `AbstractAutoProxyCreator`类实现了此接口的子接口`SmartInstantiationAwareBeanPostProcessor`，用来实现AOP，创建代理对象。
2. `CommonAnnotationBeanPostProcessor`类用来解析JSR-250注解。

## Supplier

当上面的接口没有返回的对象为`null`时，则意味着要走常规化实例化流程了。在Jdk8环境中，增加了通过`Supplier`接口实例Bean的方式。

可以通过`org.springframework.beans.factory.support.BeanDefinitionBuilder#genericBeanDefinition(java.lang.Class<T>, java.util.function.Supplier<T>)`方法来指定。

