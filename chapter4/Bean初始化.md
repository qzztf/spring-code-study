# Bean初始化过程

如果说配置文件好比菜谱，解析配置文件好比洗菜、切菜，那么Bean初始化过程就好比炒菜的过程。

## `getBean`方法 -- 炒菜开始的信号

想象一下`getBean`的场景，好比是食客点了一道菜（或者说菜单是由食客提供的，厨房代加工），厨房根据菜单做菜。想象一下在这个过程中需要做哪些工作？

1. 按照菜谱准备菜 -- 解析配置文件
1. 洗菜，切菜 - - 注册Bean定义
2. 炒菜 -- Bean 初始化
3. 添加额外的佐料 -- 动态注册Bean定义

第1，2步可以看成是同时进行的，边准备菜，边洗菜切菜也就是边解析边注册。`getBean` 方法由`BeanFactory`接口提供。

## `BeanFactory`

`BeanFactory`接口是整个Spring容器的核心接口。提供了多个获取 Bean 的方法。

- Object getBean(String name) throws BeansException;
- <T> T getBean(String name, Class<T> requiredType) throws BeansException;
- Object getBean(String name, Object... args) throws BeansException;
- <T> T getBean(Class<T> requiredType) throws BeansException;
- <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
- <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
- <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);

前三个是通过Bean的名字来获取，后面的是通过Bean的Class类型来获取。

### 通过名称获取Bean

在Spring容器中，Bean的名字(id )是唯一的。

1. 先处理名称，获取真正的Bean的名称。

    1. 如果 name 以 `&`开头，去掉`&`。

    2. 处理别名。参数有可能是别名，得跟据别名找到实际注册的 Bean 名称。

       ``` java
       //先看一下别名的结构
       // aliasA --->  name
       // aliasB ---> name
       // name ---> realName
       //name 也可以作为别名来注册，但是不能存在 name 和 alias正好相反的一对注册情况。也就是 alias --> anme, name ---> alias这样成对的出现。
       public String canonicalName(String name) {
       		String canonicalName = name;
       		// Handle aliasing...
       		String resolvedName;
       		do {
       			// 把参数当作别名来取出name, 如果能取到，说明当前参数是别名。将取到的值当作别名，继续取，如果不能取到值，那么这个就是真正的bean的名字了;如果还能取到，那么这个也是别名，重复这个过程，直到不能取到值为止。
                   resolvedName = this.aliasMap.get(canonicalName);
                   
       			if (resolvedName != null) {
       				canonicalName = resolvedName;
       			}
       		}
       		while (resolvedName != null);
       		return canonicalName;
       	}
       ```

2. 检查单例缓存中的单例。也就是说先看看有没有创建好的单例，有的就返回这个单例进一步处理。

3. 没有在单例缓存中获取到Bean，那么就检查父容器中有没有这个Bean定义。当父容器中有这个Bean定义，并且当前容器中没有此Bean定义时，就从父容器中获取。如果父容器中没有，则从当前容器获取。

4. 拿到Bean的定义，先对依赖Bean调用`getBean`方法。

5. 如果Bean定义是单例`singleton`，则创建单例。如果是原型`prototype`，则创建Bean。如果是其他的scope作用域，则调用scope的`get`方法。这里可以看出，我们可以自已定义`scope`来扩展Spring提供的几种`scope`。具体的创建Bean的过程都会调用`createBean`方法。

6. 拿到实例对象之后，如果获取时传了类型，那么将使用`TypeConverter`进行类型转换，如果转换成功，则返回这个Bean，否则将抛出异常。

### 通过Class类型获取Bean

在Spring中Bean的名称才是唯一的标识，所以根据类型获取Bean，会先获取到所有该类型的Bean 的名称。

1. 获取符合类型的Bean名称。如果有泛型的话，调用`doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit)`方法；没有泛型则调用`getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit)`方法。找到一个那就恭喜了。
2. 如果获取的Bean名称多于1个，则需要根据优先权选择出一个名称，然后再跟据这个名称去获取Bean。

### Bean实例化

Bean的创建以及实例化在`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])`方法中实现。

参数： 

- `beanName`: Bean的名称。
- `RootBeanDefinition mbd`: Bean定义。就像菜谱。
- `Object[] args`: 获取Bean时的参数

大致过程如下：

1. 调用`InstantiationAwareBeanPostProcessor`方法，这里要注意一下，**如果这这个方法返回了非Null对象，那么将不会进行实例化实际的对象，而是用这个方法返回的对象。如果有多个`InstantiationAwareBeanPostProcessor`，那么只要返回了非Null，后面的`InstantiationAwareBeanPostProcessor`就不会调用了。**`InstantiationAwareBeanPostProcessor`有两个方法。
   - 