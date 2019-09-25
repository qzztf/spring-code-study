# ObjectFactory 与BeanFactory 的区别

Spring 体系中有一些跟 Bean获取相关的接口，比如：`ObjectFactory`、`BeanFactory `、`FactoryBean`、`ObjectProvider`等。有时候会让人不知道该使用哪一个。下面具体看一下区别。

##  BeanFactory

该接口是整个Spring容器的顶层接口，定义了从容器中获取Bean的方法。当我们的应用需要从容器中获取Bean的时候用此接口。在这里我们可以将整个容器看成是一个黑盒，不需要关心它是如何获取的，我们只关心结果。

## ObjectFactory

此接口定义了一个简单工厂，是一个函数式接口，可以在调用时返回一个对象实例(可能是共享的或独立的)。

这个接口类似于`FactoryBean`，但是后者的实现类通常被定义为`BeanFactory`中的SPI实例，而该类的实现通常被作为API(通过注入)提供给其他bean。因此，getObject()方法具有不同的异常处理行为。

```java
@FunctionalInterface
public interface ObjectFactory<T> {

	/**
	 * 返回工厂管理的bean
	 * @return the resulting instance
	 * @throws BeansException in case of creation errors
	 */
	T getObject() throws BeansException;

}
```



## FactoryBean

这个接口定义了创建单个对象的工厂。如果一个bean 实现了这个接口，那么它将被用作要公开的对象的工厂，而不是直接用作将自己公开的bean 实例。

注意： **实现此接口的bean不能作为普通bean使用。`FactoryBean`是以bean形式定义的，但是引用该Bean 时是它创建的对象。**

`FactoryBean`支持单例和原型对象，可以根据需要创建对象，也可以在启动时创建对象。`SmartFactoryBean`接口可以公开更细粒度的元数据。

这个接口在Spring 框架本身中大量使用，例如在AOP模块 `org.springframework.aop.framework.ProxyFactoryBean`或`org.springframework.jndi.JndiObjectFactoryBean`中。

`FactoryBean`是一个编程规范。实现类不应该依赖于注解驱动的注入或其他反射工具。在启动过程中可能会提前调用`getObjectType() `和`getObject()`方法，甚至比后处理器都要早。如果需要访问其他bean，请实现`BeanFactoryAware`接口并通过编程获取，也就是说在`FactoryBean`中注解可能还没解析，通过注解注入其他Bean 可能不成功。

最后，`FactoryBean`对象会参与bean创建同步过程。通常不需要做内部同步，只需要在`FactoryBean`本身中进行延迟初始化。

```java
public interface FactoryBean<T> {

    /**
    * 返回此工厂对象生产的Bean
    */
	T getObject() throws Exception;

     /**
    * 返回此工厂对象生产的Bean类型
    */
	Class<?> getObjectType();
 	/**
    * 生产的Bean 是否单例
    */
	default boolean isSingleton() {
		return true;
	}

}
```



## ObjectProvider

该接口是`ObjectFactory`的变体形式，专门为注入点设计，允许编程可选性和宽松的非惟一处理。

从Spring 5.1开始，这个接口扩展了`Iterable`并提供了流支持。因此，它可以在for循环中使用，提供forEach迭代并允许流访问。

```java
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

	/**
	 * 返回该工厂管理的对象的实例(可能是共享的或独立的)。可以显式指定构造参数，类似于BeanFactory.getBean(String,Object)方法。
	 */
	T getObject(Object... args) throws BeansException;

	/**
	 * 返回该工厂管理的对象的实例(可能是共享的或独立的)。
	 */
	@Nullable
	T getIfAvailable() throws BeansException;

	/**
	 * 返回该工厂管理的对象的实例(可能是共享的或独立的)。
	 * @param defaultSupplier 回调函数，用于在工厂中不存在对象的情况下提供默认对象
	 */
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * 如果存在该实例对象的话，则消费此对象
	 * @param dependencyConsumer 在对象存在的情况下，用来消费此实例的消费者
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * 返回实例，如果没有或者不唯一的话（如有多个的情况下都没有标记为primary）则返回 null
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	@Nullable
	T getIfUnique() throws BeansException;

	/**
	 * 返回实例，如果没有或者不唯一的话（如有多个的情况下都没有标记为primary）则返回提供的默认对象
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfUnique()
	 */
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * 如果唯一的话，则消费此对象
	 * @param dependencyConsumer 如果对象唯一，则使用此消费者消费对象
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * 返回所有匹配对象实例的迭代器，不保证特定的顺序(但通常按注册的顺序)。
	 * @since 5.1
	 * @see #stream()
	 */
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * 返回所有匹配对象实例上的顺序流，不保证特定的顺序(但通常按注册的顺序)。
	 * @since 5.1
	 * @see #iterator()
	 * @see #orderedStream()
	 */
	default Stream<T> stream() {
		throw new UnsupportedOperationException("Multi element access not supported");
	}

	/**
	 * 返回所有匹配对象实例上的顺序流，根据工厂的公共顺序比较器预先排序。
在标准Spring应用程序上下文中，这将根据 org.springframework.core.Ordered 的约定，如果是基于注解的配置，也要考虑 org.springframework.core.annotation.Order 注解，类似于列表/数组类型的多元素注入点。
	 * @since 5.1
	 * @see #stream()
	 * @see org.springframework.core.OrderComparator
	 */
	default Stream<T> orderedStream() {
		throw new UnsupportedOperationException("Ordered element access not supported");
	}

```

