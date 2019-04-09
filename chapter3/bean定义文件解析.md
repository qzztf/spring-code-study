# Bean 定义文件的解析

## 初始化工厂

```java
// 1. 初始化一个bean 工厂
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
// 2. 初始化XmlBeanDefinitionReader,负责从xml文件中读取bean定义
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
// 3. 加载bean 定义的入口方法
reader.loadBeanDefinitions("classpath:app.xml");
```

上述代码使用起来很简单，创建一个工厂和阅读器，传入配置文件的位置。

我们可以考虑一下，这些代码做了什么。

1. XmlBeanDefinitionReader 是如何定位配置文件的?
2. 如何解析配置文件的？
3. XmlBeanDefinitionReader的构造函数传入了一个BeanDefinitionRegistry对象，这个对象是用来注册bean定义的，那么是如何注册的呢？

## ResourceLoader 定位资源文件

### Resource

Spring将各种资源统一抽象为`Resource`,并使用`ResourceLoader`来定位`Resource`。如果资源以物理形式存在，那么它可以为每个资源打开一个InputStream，但是URL或文件句柄只能由某些特定资源返回。实际的行为取决于实现类。

#### `Resource`的结构

下面是Resource的类关系图

![resource类关系图](.bean定义文件解析_images/resource类关系图.png)

下面是Resource的类结构图

![resource结构](.bean定义文件解析_images/resource结构.png)

`Resource`继承自`InputStreamSource`, 得到了获取`InputStream`的能力，可以以流的形式读取资源文件。

定义的方法：

- boolean exists()：确定此资源是实际物理存在。此方法执行确定的存在性检查，而资源句柄的存在仅保证有效的描述符句柄。
- boolean isReadable()：指示是否可以通过`getInputStream()`读取此资源的非空内容。对于存在的典型资源描述符会返回`true`，因为从5.1开始它严格地暗示了`exist()`语义。请注意，在尝试读取实际内容时仍然可能失败。但是，如果返回值为`false`，则表示资源内容无法读取。
- boolean isOpen(): 指示此资源是否表示具有打开流的句柄。如果为`true`，则不能多次读取InputStream，必须读取并关闭该资源，以避免资源泄漏。对于典型的资源描述符将为false。
- boolean isFile()：确定此资源是否表示文件系统中的文件。返回`true`标志着(但不保证)`getFile()`将调用成功。
这在默认情况下返回`false`.
- URL getURL(): 返回此资源的URL句柄。
- URI getURI(): 返回此资源的URI句柄。
- File getFile(): 返回此资源的文件句柄。如果资源不能被解析为绝对文件路径，也就是说，如果资源在文件系统中不可用将抛出`FileNotFoundException`异常。
- ReadableByteChannel readableChannel()：返回一个`ReadableByteChannel`。希望每次调用都创建一个新的通道。
默认实现返回`Channels.newChannel(getInputStream())`。
- long contentLength()：确定此资源的内容长度。
- long lastModified()：确定此资源最后修改的时间戳。
- Resource createRelative(String relativePath)：创建与此资源相对路径的资源。
- String getFilename()：确定此资源的文件名，通常是路径的最后一部分:例如，“myfile.txt”。如果此类资源没有文件名，则返回`null`。
- String getDescription(): 返回此资源的描述，用于处理该资源时的错误输出。实现类可以从它们的`toString`方法返回这个值。

#### Resource 的子接口

##### ContextResource

用于从封闭的“上下文”(例如从`javax.servlet.ServletContext`)加载资源的扩展接口，也可以从普通类路径或相对于文件系统路径(没有显式指定前缀，因此应用于相对于本地`ResourceLoader`的上下文)。

定义的方法：

- String getPathWithinContext()：返回包含在“上下文”中的路径。通常是相对于上下文特定的根目录的路径，例如`ServletContext`根目录或`PortletContext`根目录。

##### WritableResource

支持对资源进行写入的扩展接口。该接口提供一个`OutputStream`访问器。

定义的方法：

- boolean isWritable()：指示是否可以通过`getOutputStream()`向此资源写入内容。适用于典型的资源描述符;注意，实际在尝试写入时仍然可能失败。但是，如果值为false，则表示资源内容不能修改
- OutputStream getOutputStream()：返回底层资源的`OutputStream`，允许(过度)写入其内容.
- WritableByteChannel writableChannel(): 返回一个`WritableByteChannel`。希望每个调用都创建一个新的通道。默认实现返回`Channels.newChannel(getOutputStream())`。

##### HttpResource

将资源写入HTTP响应的扩展接口

定义的方法：

- HttpHeaders getResponseHeaders(): 服务于当前资源的HTTP响应头。

#### 常用的`Resource`实现类

##### ClassPathResource

类路径资源的实现类。使用给定的类加载器或给定的类来加载资源。如果类路径资源驻留在文件系统中，而不是JAR中的资源，则支持`java.io.File`方式来加载。该类始终支持URL方式来加载。

使用示例：

```java
ClassPathResource resource = new ClassPathResource("app.xml");
System.out.println("资源文件是否存在：" + resource.exists());
System.out.println("资源文件是否是文件：" + resource.isFile());
System.out.println("资源文件是否可读：" + resource.isReadable());
System.out.println("资源文件名称：" + resource.getFilename());
System.out.println("资源文件：" + resource.getFile());
System.out.println("资源文件描述：" + resource.getDescription());
System.out.println("资源文件URL：" + resource.getURL());
System.out.println("资源文件URI：" + resource.getURI());
System.out.println("资源文件长度：" + resource.contentLength());
System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));
Resource resourceRelative = resource.createRelative("relative.xml");
System.out.println("相对路径资源：" + resourceRelative);
System.out.println("相对路径资源是否存在：" + resourceRelative.exists());
System.out.println("相对路径资源文件：" + resourceRelative.getFile());

//输出结果
资源文件是否存在：true
资源文件是否是文件：true
资源文件是否可读：true
资源文件名称：app.xml
资源文件：F:\spring-code-study\chapter3\target\classes\app.xml
资源文件描述：class path resource [app.xml]
资源文件URL：file:/F:/spring-code-study/chapter3/target/classes/app.xml
资源文件URI：file:/F:/spring-code-study/chapter3/target/classes/app.xml
资源文件长度：307
资源文件最后修改时间：Mon Apr 08 10:40:17 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
相对路径资源：class path resource [relative.xml]
相对路径资源是否存在：false
Exception in thread "main" java.io.FileNotFoundException: class path resource [relative.xml] cannot be resolved to URL because it does not exist
	at org.springframework.core.io.ClassPathResource.getURL(ClassPathResource.java:195)
	at org.springframework.core.io.AbstractFileResolvingResource.getFile(AbstractFileResolvingResource.java:150)
	at cn.sexycode.spring.study.chapter3.ClassPathResourceDemo.main(ClassPathResourceDemo.java:30)

```

##### FileSystemResource

处理`java.io.File`和`java.nio.file.Path`文件系统的资源实现。支持作为一个文件，也可以作为一个URL。扩展自`WritableResource`接口。

**注意:从Spring Framework 5.0开始，这个资源实现使用NIO.2 API进行读/写交互。从5.1开始，它可能使用一个`java.nio.file.Path`句柄实例化，在这种情况下，它将通过NIO.2执行所有文件系统交互，
只能通过`getFile()`来返回`File`**


### ResourceLoader

加载资源的策略接口(例如类路径或文件系统资源)。`org.springframework.context.ApplicationContext`需要提供此功能，以及扩展`org.springframework.core.io.support.ResourcePatternResolver`支持。
`DefaultResourceLoader`是一个独立的实现，可以在`ApplicationContext`之外使用，也可以由`ResourceEditor`使用。
当在`ApplicationContext`中使用时，可以使用特定上下文的资源加载策略从字符串填充Resource类型和Resource数组的Bean属性。

ResourceLoader提供的方法：

- Resource getResource(String location)：返回指定资源位置的资源句柄。
句柄应该始终是一个可重用的资源描述符，允许多次调用`Resource#getInputStream()`。
必须支持完全限定的url，例如。“file:C:/test.dat”。
必须支持类路径伪url，例如。“classpath:test.dat”。
应该支持相对文件路径，例如。“WEB-INF/test.dat”。(这是基于特定实现的，通常由`ApplicationContext`实现类提供。)
**注意，资源句柄并不意味着资源实际存在;还是需要调用`Resource#exists`检查资源是否存在**

#### `ResourcePatternResolver`子接口

用于将位置模式(例如，ant样式的路径模式)解析为资源对象的策略接口。
这是ResourceLoader接口的扩展。可以检查传入的ResourceLoader(例如，当运行在上下文中时，通过`ResourceLoaderAware`传入的`ApplicationContext`)是否也实现了这个接口。
`PathMatchingResourcePatternResolver`是一个独立的实现，可以在ApplicationContext之外使用，`ResourceArrayPropertyEditor`也使用它来填充bean的`Resource` 数组属性。
可以与任何类型的位置模式(例如`/WEB-INF/*-context.xml`):输入模式必须匹配策略实现。这个接口只指定转换方法，而不是特定的模式格式。
这个接口还为类路径中所有匹配的资源提供了一个新的资源前缀`classpath*:`。注意，在这种情况下，资源位置应该是一个没有占位符的路径(例如 `/beans.xml`);JAR文件或类目录可以包含多个同名文件

此接口提供的方法：

- Resource[] getResources(String locationPattern)：将给定的位置模式解析为资源对象。应该尽可能避免指向相同物理资源的重复资源项。结果应该具有set语义

#### 常用实现类

##### DefaultResourceLoader

`ResourceLoader`接口的默认实现。由`ResourceEditor`使用，并作为`org.springframework.context.support.AbstractApplicationContext`的基类。也可以单独使用。
如果位置值是URL，则返回UrlResource;如果非URL路径或“classpath:”伪URL，则返回ClassPathResource。