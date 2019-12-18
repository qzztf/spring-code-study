# Spring 创建Bean的方式

在Spring创建Bean时，会遵循下面的顺序来创建。这个顺序是`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])`方法的内在逻辑。

1. `InstantiationAwareBeanPostProcessor`
2. `Instance Supplier`
3. 工厂方法
4. 构造函数
5. 默认构造函数

##  InstantiationAwareBeanPostProcessor

`InstantiationAwareBeanPostProcessor`接口在实例化Bean之前和实例化Bean之后设置属性或者自动装配之前调用。通常用于阻止特定目标bean的默认实例化行为，例如创建具有特定目标对象的代理(池目标、延迟初始化目标等)，或者实现额外的注入策略，如字段注入。
**注意**: 此接口为专用接口，主要供框架内部使用。建议尽可能实现简单的`BeanPostProcessor`接口，或者继承`InstantiationAwareBeanPostProcessorAdapter`，以避免扩展这个接口导致误操作。

在Spring中有

1. `AbstractAutoProxyCreator`类实现了此接口的子接口`SmartInstantiationAwareBeanPostProcessor`，用来实现AOP，创建代理对象。
2. `CommonAnnotationBeanPostProcessor`类用来解析JSR-250注解。

## Supplier

当上面的接口没有返回的对象为`null`时，则意味着要走常规化实例化流程了。在Jdk8环境中，增加了通过`Supplier`接口实例化Bean的方式。

可以通过`org.springframework.beans.factory.support.BeanDefinitionBuilder#genericBeanDefinition(java.lang.Class<T>, java.util.function.Supplier<T>)`方法来指定。这种方式适用于手动注册Bean。

## 工厂方法

此种方式使用命名工厂方法实例化Bean。该工厂可能是静态方法（通过Bean定义参数指定了一个类）也可能是工厂Bean。

实际中需要遍历在`RootBeanDefinition`中指定的静态方法或实例方法(方法可能被重载)名称，并尝试与参数匹配。我们没有将类型附加到构造函数args，因此尝试和错误是唯一的方法。getBean方法传入的参数也会传递到这一步中。这一步难就难在可能存在同名的方法，那么就需要去做匹配。

当备选方法有多个时，就需要猜测用的是哪个方法。可以想象一下影响因素：*首先肯定是方法名，其次是传进来的参数类型，顺序，最后是返回值。* 这里的过程有点儿复杂，类似于我们在写代码时，编绎器为什么可以知道我们调用的是哪一个方法？可以根据方法名，参数类型，顺序明确知道是哪一个方法（实际编绎器做了哪些操作，有兴趣的可以查资料）。

1. 先将方法按照参数个数排序，参数多的靠前。也就是说参数越多，优先级越高。
2. 遍历每个方法，按照方法参数索引逐个比较参数名称、类型是否匹配。我们可以通过`<constructor-arg name="name" value="1"/>`标签来指定参数， 这一步的匹配会先根据`name`和`type`来判断是否匹配，如果不匹配再判断是不是可以自动注入；匹配上则会尝试类型转换，如果转换出错，也代表着不匹配了。不匹配再尝试下一个方法。匹配上的方法意味着这个方法很有可能就是我们想要调用的方法，再判断一下方法参数与解析出来的参数之间的差异值（Spring 计算出来的一个值，根据方法参数类型和实际参数类型），当有多个匹配方法时，这个值越小，代表越匹配。当我们在`getBean`时，如果传了构造参数，那么参数的个数需要完全匹配。如果是通过xml配置的构造参数，则不要求个数完全一致，在上述遍历的过程中，会找到最匹配的方法。

当*构造函数设置宽松模式（默认）时，可能会出现上述所说的多个方法的差异值计算出来相同，这种情况下，会选择最先匹配的方法；严格模式时，则会抛出异常，让我们确认使用哪一个。*

举个例子：

有如下两个工厂方法：

```java
public Bean getBean(Long name){
    return new Bean();
}
public Bean getBean(Integer name){
    return new Bean();
}
```

如下配置：

```java
<bean id="bean" class="cn.sexycode.spring.study.chapter4.FactoryBeanBean$Bean" factory-bean="factoryBeanBean" factory-method="getBean">
    <constructor-arg name="name" value="1"/>
</bean>
```

如果配置参数时，不指定类型，则会进行类型转换，这个时候上面的两个方法都将会匹配上。如果是构造函数宽松模式，那么将会使用其中一个（跟方法声明顺序有关）。如果是严格模式，则会报错，需要指定参数类型。

找到对应的方法之后，就可去实例化对象了。在这里Spring并没有简单的调用构造方法去实例化，而是提供了一个策略接口`InstantiationStrategy`，调用对应的实现类去实例化。默认提供了两个实现类`SimpleInstantiationStrategy`和`CglibSubclassingInstantiationStrategy`。前面一个会直接调用对应的方法实例化，后者则会通过cglib实例化一个子类，注入相关的方法回调。

## 构造函数

构造函数的方式与工厂方法极其相似，根据参数找到匹配的构造函数（也是一种方法），后面的流程跟工厂方法相同。不过在这一步中构造方法的获取有所不同。先通过org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors方法获取候选构造函数，通过此接口来支持构造函数自动装配，比如`@Autowired`注解标记的构造函数。如果没有找到特定的构造函数，则会遍历所有的构造进行匹配。注*`@Autowired`*标注的构造函数只能有一个，如果有这样的构造函数，也会优先使用。

在多个方法匹配的时候，Spring 会做到尽量更接近我们想要使用的方法。我们有时候为了偷懒，会省略一些配置参数，比如`<constructor-arg/>`标签只写`name`和`value`，虽然也可以达到我们的目的，但是Spring为我们多做了一些匹配的工作，也有可能在匹配的过程中不是我们想要的（情况较少）。我们应该尽可能的提供更多的信息，尽可能准确匹配。这样既能提高准确率，也能提高速度。就好比，追女朋友的时候，她有多个想要的礼物，你只能买一个，如果你能从她那里得到更多的提示，那不是能更准确地送出她最想要的礼物吗？不然就只能靠猜了。貌似也有点儿AI的味道在里面？

## 默认构造函数

上面几种方式都没有实例化的话，那只能走默认构造函数来初始化了。实例化的时候也会使用到`InstantiationStrategy`接口。虽然只是简单的调用构造函数来实例化对象，Spring也比我们考虑的多。当Bean定义有方法覆盖时，必须得使用到cglib为生成子类了，注入相关的方法回调。

## 写在最后

这一篇写了挺长时间的，主要是因为方法匹配的情况挺多的，要把每种情况都覆盖到，也不太现实。有时候得边写边调试代码。





