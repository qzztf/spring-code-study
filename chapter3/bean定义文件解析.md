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

这里提到了URL, URL 是用来在互联网中描述一个资源位置的，主要包括协议，主机，路径等组成部分。

Spring 支持的URL:

- classpath: 表示classpath下的资源文件
- file： 表示文件系统中的资源文件
- jar： 表示jar包中的资源文件
- war： 表示war包中的资源文件
- zip：zip包中的资源文件
- wsjar： WebSphere jar文件
- vfszip：JBoss jar文件
- vfsfile：JBoss 文件系统
- vfs：通用的JBoss VFS 文件

使用示例：

```java
DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
//读取file文件
System.out.println("------------------------------------读取本地文件------------------" );
Resource resource = resourceLoader.getResource("file:///F:\\spring-code-study\\chapter3\\target\\classes/app.xml");
System.out.println("资源文件是否存在：" + resource.exists());
System.out.println("资源文件是否是文件：" + resource.isFile());
System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
System.out.println("资源文件名称：" + resource.getFilename());
System.out.println("资源文件：" + resource.getFile());
System.out.println("资源文件描述：" + resource.getDescription());
System.out.println("资源文件URL：" + resource.getURL());
System.out.println("资源文件URI：" + resource.getURI());
System.out.println("资源文件长度：" + resource.contentLength());
System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

//读取jar包中的文件
System.out.println("------------------------------------读取jar文件----------------------" );
resource = resourceLoader.getResource("jar:file:///F:\\spring-code-study\\chapter3\\target\\chapter3-1.0-SNAPSHOT.jar!/app.xml");
System.out.println("资源文件是否存在：" + resource.exists());
System.out.println("资源文件是否是文件：" + resource.isFile());
System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
System.out.println("资源文件名称：" + resource.getFilename());
//        System.out.println("资源文件：" + resource.getFile());
System.out.println("资源文件描述：" + resource.getDescription());
System.out.println("资源文件URL：" + resource.getURL());
//        System.out.println("资源文件URI：" + resource.getURI());
System.out.println("资源文件长度：" + resource.contentLength());
System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

//读取网络文件
System.out.println("------------------------------------读取网络文件-----------------" );
resource = resourceLoader.getResource("https://raw.githubusercontent.com/qzzsunly/spring-code-study/master/chapter3/src/main/resources/app.xml");
System.out.println("资源文件是否存在：" + resource.exists());
System.out.println("资源文件是否是文件：" + resource.isFile());
System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
System.out.println("资源文件名称：" + resource.getFilename());
//        System.out.println("资源文件：" + resource.getFile());
System.out.println("资源文件描述：" + resource.getDescription());
System.out.println("资源文件URL：" + resource.getURL());
//        System.out.println("资源文件URI：" + resource.getURI());
System.out.println("资源文件长度：" + resource.contentLength());
System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));


//读取classpath下的文件
System.out.println("------------------------------------读取classpath文件-----------------" );
resource = resourceLoader.getResource("classpath:app.xml");
System.out.println("资源文件是否存在：" + resource.exists());
System.out.println("资源文件是否是文件：" + resource.isFile());
System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
System.out.println("资源文件名称：" + resource.getFilename());
//        System.out.println("资源文件：" + resource.getFile());
System.out.println("资源文件描述：" + resource.getDescription());
System.out.println("资源文件URL：" + resource.getURL());
//        System.out.println("资源文件URI：" + resource.getURI());
System.out.println("资源文件长度：" + resource.contentLength());
System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

```

测试结果：

```plain
------------------------------------读取本地文件------------------
资源文件是否存在：true
资源文件是否是文件：true
资源文件是否可读：true
资源文件名称：app.xml
资源文件：F:\spring-code-study\chapter3\target\classes\app.xml
资源文件描述：URL [file:/F:/spring-code-study/chapter3/target/classes/app.xml]
资源文件URL：file:/F:/spring-code-study/chapter3/target/classes/app.xml
资源文件URI：file:/F:/spring-code-study/chapter3/target/classes/app.xml
资源文件长度：307
资源文件最后修改时间：Mon Apr 08 10:40:17 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
------------------------------------读取jar文件----------------------
资源文件是否存在：true
资源文件是否是文件：false
资源文件是否可读：true
资源文件名称：app.xml
资源文件描述：URL [jar:file:///F:\spring-code-study\chapter3\target\chapter3-1.0-SNAPSHOT.jar!/app.xml]
资源文件URL：jar:file:///F:\spring-code-study\chapter3\target\chapter3-1.0-SNAPSHOT.jar!/app.xml
资源文件长度：307
资源文件最后修改时间：Wed Apr 10 09:05:03 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
------------------------------------读取网络文件-----------------
资源文件是否存在：true
资源文件是否是文件：false
资源文件是否可读：true
资源文件名称：app.xml
资源文件描述：URL [https://raw.githubusercontent.com/qzzsunly/spring-code-study/master/chapter3/src/main/resources/app.xml]
资源文件URL：https://raw.githubusercontent.com/qzzsunly/spring-code-study/master/chapter3/src/main/resources/app.xml
资源文件长度：302
资源文件最后修改时间：Thu Jan 01 08:00:00 CST 1970
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
------------------------------------读取classpath文件-----------------
资源文件是否存在：true
资源文件是否是文件：true
资源文件是否可读：true
资源文件名称：app.xml
资源文件描述：class path resource [app.xml]
资源文件URL：file:/F:/spring-code-study/chapter3/target/classes/app.xml
资源文件长度：307
资源文件最后修改时间：Mon Apr 08 10:40:17 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
```

从上面的例子中可以看到使用`ResourceLoader`和`Resource`接口，我们可以很方便的读取文件内容。

##### PathMatchingResourcePatternResolver

此实现支持ant风格的通配符形式路径。在有多个文件的情况下，我们可以利用这个类来加载多个配置文件。

###### 在没有通配符的情况

如果指定的路径没有`classpath*:`前缀，将通过底层ResourceLoader的getResource()返回单个的资源。比如“file:C:/context”。“classpath:/context”这样的伪url，以及简单的无前缀路径，如“/WEB-INF/context.xml”，后者将基于底层ResourceLoader的方式解析(例如，用于WebApplicationContext的ServletContextResource)。

###### Ant 风格的路径

当路径包含一个ant样式的模式时，例如:`/WEB-INF/*-context.xml`、`com/mycompany/**/applicationContext.xml`、`file:C:/some/path/*-context.xml`、`classpath:com/mycompany/**/applicationContext.xml`, 解析器遵循一个更复杂但是已定义好的算法来尝试解析通配符。它为最后一个非通配符部分的路径生成一个资源，并获取一个URL。如果该URL不是`jar:` URL或特定于容器的变体(例如WebLogic中的`zip:`，WebSphere中的`wsjar`等等)，那么将从它获取`java.io.File`，并通过遍历文件系统来解析通配符。对于jar URL，解析器要么从中获取java.net.JarURLConnection，要么手动解析jar URL，然后遍历jar文件的内容来以解析通配符

###### 可移植性影响

如果指定的路径已经是文件URL(由于基本ResourceLoader是基于文件系统的，所以可以显式或隐式地使用)，那么通配符就可以方便移植。如果指定的路径是类路径，那么解析器必须通过调用`Classloader.getResource()`获得最后一个非通配符路径部分的URL。由于这只是路径的一个节点(而不是末尾的文件)，所以它实际上是未定义的(在ClassLoader 的文档中有提及)。实际上，它通常是一个表示目录的`java.io.File`，其中类路径资源解析为文件系统位置，或者是某种jar URL，其中类路径解析为jar所在位置。尽管如此，这个操作仍然存在可移植性问题。为了从最后一个非通配符部分获取jar URL，解析器必须能够从中获得`java.net.JarURLConnection`，或者手动解析jar URL，以便能够遍历jar的内容并解析通配符。这将在大多数环境中工作，但在其他环境中可能会失败，强烈建议在依赖jar之前，在你的特定环境中彻底测试jar包资源的通配符解析。

###### classpath*:前缀

支持通过`classpath*:`前缀检索同名的多个类路径资源。例如,`classpath*:META-INF/beans.xml`将找到classpath下所有的`beans.xml`文件，无论是在`classes`目录中还是在JAR文件中。这对于自动检测每个jar文件中的相同位置的相同名称的配置文件特别有用。在内部是通过`ClassLoader.getResources()`方法实现的，并且是完全可移植的。`classpath*:`前缀还可以结合`PathMatcher` 模式，例如`classpath*:META-INF/*-beans.xml`。在本例中，解析策略相当简单:在最后一个非通配符路径部分上调用`ClassLoader.getResources()`来获取类加载器层次结构中所有匹配的资源，然后在每个资源之外使用上面描述的相同路径匹配器解析策略来处理通配符子路径。

###### 另注

警告: 注意，`classpath*:`与ant样式的模式结合使用时，根目录在文件系统中必须存在。这意味着像`classpath*:*.xml`这样的模式不会从jar文件的根中检索文件，而是只从扩展目录的根中检索。这源于JDK的`ClassLoader.getResources()`方法中的一个限制，该方法传入空字符串只返回文件系统位置(指示潜在的要搜索的根目录)。这个`ResourcePatternResolver`实现试图通过URLClassLoader自省和`java.class.path`清单来减轻jar根目录查找的限制;但是不保证可移植性。
**警告: 当`classpath:`搭配Ant风格时，如果在多个类路径位置都能搜索到根包，则不能保证资源能够找到匹配的资源。这是因为如
`com/mycompany/package1/service-context.xml`这样的资源可能只在一个位置，但当尝试解析这样路径时:`com/mycompany/**/service-context.xml`,解析器在处理`getResource(com/mycompany”)`返回的(第一个)URL，如果此存在于多个类加载器时，则实际的想要的资源可能不在返回的类加载器中。因此，在这种情况下，最好使用具有相同ant样式模式的`classpath*:`，它将搜索包含根包的所有类路径。**

###### 使用示例

```java

PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
//读取file文件
System.out.println("------------------------------------读取多个文件------------------");
Resource[] resources = resourceLoader.getResources("classpath:app*.xml");
for (Resource resource : resources) {

    System.out.println("资源文件是否存在：" + resource.exists());
    System.out.println("资源文件是否是文件：" + resource.isFile());
    System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
    System.out.println("资源文件名称：" + resource.getFilename());
    System.out.println("资源文件：" + resource.getFile());
    System.out.println("资源文件描述：" + resource.getDescription());
    System.out.println("资源文件URL：" + resource.getURL());
    System.out.println("资源文件URI：" + resource.getURI());
    System.out.println("资源文件长度：" + resource.contentLength());
    System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
    System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

}
    
```

结果：

```text
------------------------------------读取多个文件------------------
资源文件是否存在：true
资源文件是否是文件：true
资源文件是否可读：true
资源文件名称：app0.xml
资源文件：F:\spring-code-study\chapter3\target\classes\app0.xml
资源文件描述：file [F:\spring-code-study\chapter3\target\classes\app0.xml]
资源文件URL：file:/F:/spring-code-study/chapter3/target/classes/app0.xml
资源文件URI：file:/F:/spring-code-study/chapter3/target/classes/app0.xml
资源文件长度：331
资源文件最后修改时间：Wed Apr 10 20:57:17 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!--配置文件0-->
</beans>
资源文件是否存在：true
资源文件是否是文件：true
资源文件是否可读：true
资源文件名称：app1.xml
资源文件：F:\spring-code-study\chapter3\target\classes\app1.xml
资源文件描述：file [F:\spring-code-study\chapter3\target\classes\app1.xml]
资源文件URL：file:/F:/spring-code-study/chapter3/target/classes/app1.xml
资源文件URI：file:/F:/spring-code-study/chapter3/target/classes/app1.xml
资源文件长度：331
资源文件最后修改时间：Wed Apr 10 10:46:52 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!--配置文件1-->
</beans>
资源文件是否存在：true
资源文件是否是文件：true
资源文件是否可读：true
资源文件名称：app2.xml
资源文件：F:\spring-code-study\chapter3\target\classes\app2.xml
资源文件描述：file [F:\spring-code-study\chapter3\target\classes\app2.xml]
资源文件URL：file:/F:/spring-code-study/chapter3/target/classes/app2.xml
资源文件URI：file:/F:/spring-code-study/chapter3/target/classes/app2.xml
资源文件长度：331
资源文件最后修改时间：Wed Apr 10 10:46:50 CST 2019
资源文件数据：<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!--配置文件2-->
</beans>
```

## BeanDefinitionReader 加载bean定义资源

通过`ResourceLoader` 定位资源文件之后，那么就该`BeanDefinitionReader`开始着手加载bean定义了。

定义的方法：

- int loadBeanDefinitions(Resource resource)：从单个资源中加载，返回发现的bean 定义数量
- int loadBeanDefinitions(Resource... resources)：从多个资源中加载
- int loadBeanDefinitions(String location)：从单个资源位置加载
- int loadBeanDefinitions(String... locations)：从多个资源位置加载

从方法定义中可以看出，`BeanDefinitionReader`提供了多个从各种资源中加载bean定义的功能。

### 实现类

Spring 提供了三个实现类，用于从不同格式的资源文件中加载bean定义。

#### PropertiesBeanDefinitionReader

该类用于从properties文件中加载bean定义。

#### GroovyBeanDefinitionReader

基于Groovy的 reader。
这个bean定义reader还可以读取XML bean定义文件，允许与Groovy bean定义文件无缝搭配。

#### XmlBeanDefinitionReader

读取xml bean定义文件，将资源解析成xml w3c document, 内部委托给`BeanDefinitionDocumentReader`接口(具体实现类是`DefaultBeanDefinitionDocumentReader`)去加载。

```java
protected void doRegisterBeanDefinitions(Element root) {
		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.
		//任何内部的 <beans> 标签，将会递归调用这个方法， 为了正确地传播和保存<beans> 的`default-`属性值，需要跟踪当前(父)委托，它有可能为空。
		//因为子beans 需要继承父beans的一些默认属性，比如说default-lazy-init="default", 
		//创建新的(子)委托，并使用父委托的引用进行回退，然后最终将this.delegate重置为其原始(父)引用。这种行为模拟了一堆委托，实际上并不需要委托。
		BeanDefinitionParserDelegate parent = this.delegate;
		// 为当前根节点创建新的委托解析器
		this.delegate = createDelegate(getReaderContext(), root, parent);

        //判断当前标签是不是在默认命名空间中，默认命名空间代表<benas>标签所在的命名空间
		if (this.delegate.isDefaultNamespace(root)) {
		    //当前<beans> 标签指定了profile属性，则跟当前环境中激活的profile进行比较，如果是未激活，则跳过当前<beans> 标签
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				// We cannot use Profiles.of(...) since profile expressions are not supported
				// in XML config. See SPR-12458 for details.
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		preProcessXml(root);
		
		//解析当前<beans>标签
		parseBeanDefinitions(root, this.delegate);
		postProcessXml(root);

		this.delegate = parent;
	}
```

BeanDefinitionParserDelegate 对象是用来具体解析每个元素的，为什么需要创建一个新的。可以

再看看parseBeanDefinitions方法
```java
//如果当前元素属于默认命名空间，这里其实就是指<beans>标签
if (delegate.isDefaultNamespace(root)) {
    NodeList nl = root.getChildNodes();
    //遍历子节点
    for (int i = 0; i < nl.getLength(); i++) {
        Node node = nl.item(i);
        if (node instanceof Element) {
            Element ele = (Element) node;
            if (delegate.isDefaultNamespace(ele)) {
                //如果该标签属性默认命名空间，Uri为空，或者是 http://www.springframework.org/schema/beans
                // import标签
                if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
                    // 先处理路径中的点位符，如：${user.dir}
                    // 判断引入的资源是否绝对路径(classpath:, classpath*:, url资源)，再调用reader的loadBeanDefinitions 方法
                    // 相对路径则使用 当前的资源创建相对路径资源（getReaderContext().getResource().createRelative(location)）
                    //再调用reader的loadBeanDefinitions 方法
                    importBeanDefinitionResource(ele);
                }
                //alias 标签
                else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
                    //调用org.springframework.core.AliasRegistry.registerAlias 方法注册别名
                    processAliasRegistration(ele);
                }
                // bean 标签
                else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
                    processBeanDefinition(ele, delegate);
                }
                // 内部 beans 标签， 则递归调用 doRegisterBeanDefinitions()方法，
                else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
                    // recurse
                    doRegisterBeanDefinitions(ele);
                }
            }
            else {
                delegate.parseCustomElement(ele);
            }
        }
    }
}
else {
    delegate.parseCustomElement(root);
}
```

主要看一下 processBeanDefinition 方法，这个方法用来解析bean定义。
processBeanDefinition(): 
```java
//调用了BeanDefinitionParserDelegate 来解析出BeanDefinitionHolder对象，这个对象中封装了 BeanDefinition 以及bean名称对象。
//BeanDefinition 是用来描述bean 定义的对象，主要包括 class, lazy-init ，依赖对象， 初始化方法，销毁方法，作用域, 父级bean定义等信息，这些信息都解析自xml bean标签，一一对应。

BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
if (bdHolder != null) {
    bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
    try {
        // Register the final decorated instance.
        BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
    }
    catch (BeanDefinitionStoreException ex) {
        getReaderContext().error("Failed to register bean definition with name '" +
                bdHolder.getBeanName() + "'", ele, ex);
    }
    // Send registration event.
    getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
}
```
下面看一下BeanDefinitionParserDelegate的parseBeanDefinitionElement方法

```java

public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
		String id = ele.getAttribute(ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

		List<String> aliases = new ArrayList<>();
		if (StringUtils.hasLength(nameAttr)) {
		    // 解析bean name， 可以有多个，用 ,;  隔开
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}


		String beanName = id;
		// id 为空，则用第一个name 作为id
		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = aliases.remove(0);
			if (logger.isTraceEnabled()) {
				logger.trace("No XML 'id' specified - using '" + beanName +
						"' as bean name and " + aliases + " as aliases");
			}
		}

		if (containingBean == null) {
		    //判断名称是否唯一
			checkNameUniqueness(beanName, aliases, ele);
		}

        // 具体解析标签的每个元素，设置 beanDefinition对应的属性
		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName = BeanDefinitionReaderUtils.generateBeanName(
								beanDefinition, this.readerContext.getRegistry(), true);
					}
					else {
						beanName = this.readerContext.generateBeanName(beanDefinition);
						// Register an alias for the plain bean class name, if still possible,
						// if the generator returned the class name plus a suffix.
						// This is expected for Spring 1.2/2.0 backwards compatibility.
						String beanClassName = beanDefinition.getBeanClassName();
						if (beanClassName != null &&
								beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
								!this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
							aliases.add(beanClassName);
						}
					}
					if (logger.isTraceEnabled()) {
						logger.trace("Neither XML 'id' nor 'name' specified - " +
								"using generated bean name [" + beanName + "]");
					}
				}
				catch (Exception ex) {
					error(ex.getMessage(), ele);
					return null;
				}
			}
			String[] aliasesArray = StringUtils.toStringArray(aliases);
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}

		return null;
	}
```