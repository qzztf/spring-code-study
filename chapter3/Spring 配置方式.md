# Spring 配置方式

从Spring诞生开始，用的是XML配置形式，这在那个年代是常用的方式。修改XML不需要重新编绎class文件，但是由于没有校验机制，配置正确于否，只能在程序运行的时候才能知晓。灵活性也欠佳，无法实现复杂逻辑。当Java 5推出注解之后，Spring 又推出了基于注解的配置方式，可以达到在编绎期就能判断配置是否正确的目的，但是修改注解之后，需要重新编绎class。又在注解的基础上推出了编程式的配置方式，将灵活性推到了另高的层次。Spring还有两种用得较少的配置方式：一是`properties`文件，这种单行文件配置起来异常复杂，也不方便理解，实际应用不多。二是groovy脚本的形式，这种形式也可以非常灵活并且很简洁，不过熟悉java语法的朋友可能还需要熟悉一下。

## XML形式

这种形式在配置文件的解析一文已写，通过`XmlBeanDefinitionReader` 类来读取xml配置文件并解析。

# Properties形式

通过`PropertiesBeanDefinitionReader`来读取配置文件并解析。

# Groovy 脚本形式

通过`GroovyBeanDefinitionReader`来读取配置文件并解析。

# 注解形式

之前在《bean定义文件解析》一文中提到`ComponentScanBeanDefinitionParser`用来解析`<context:component-scan/>`标签，在解析的过程中会根据规则如注解，`package`，`type`等来扫描对应的注解class，可以设置多个package，用`,; \t\n`进行分割。默认会扫描`@Component`注解的类，也可以配置自己的类型过滤器。该标签有个属性`annotation-config`默认为`true`，在解析的时候会注册`ConfigurationClassPostProcessor`后处理器。但是这个标签无法对对象属性进行装配，需要结合`<context:annotation-config/>`标签来实现完整的注解配置。从Spring 5 开始默认扫描`@Indexed`注解，`@Component`也加上了该注解，因此也会扫描到。

# Java 编程形式

之前在《bean定义文件解析》一文中提到`AnnotationConfigBeanDefinitionParser`用来解析`<context:annotation-config/>`标签，在解析的过程中会注册`ConfigurationClassPostProcessor`，此类实现`BeanDefinitionRegistryPostProcessor`接口，可以在初始化bean之前修改bean定义。

1. 从已被注册的bean定义中筛选`@Configuration`配置类以及轻量级配置类`@Component`、`@ComponentScan` 、`@Import`、`@ImportResource`，以及方法级注解`@Bean`。这里有一点要提一下：`@Configuration`注解也被``@Component`注解修饰，换句话说*被`@Configuration`注解的类也会注册为bean*。
2. 筛选出上述配置类后，接下来需要解析这些类。具体会交给`org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitions`方法。在此方法会先校验是否需要跳过加载该配置类（这里包括了上述的轻量级配置类），主要通过`@Conditional`注解（4.0新增，Spring Boot用来实现条件注册的核心）来实现，如果需要跳过，则将此Bean定义移除。然后解析该类上的`@Import`注解、`@Bean`注解、`@ImportResource`注解，加载`ImportBeanDefinitionRegistrar`。
3. *在第2步中，新注册了一些Bean定义，这些Bean定义中也有可能是配置类，那么在新增的Bean定义中筛选出新的配置类，再来一次第2步。*
4. 直到没有新增加的配置类。

在此标签的解析过程中还会注册`AutowiredAnnotationBeanPostProcessor`后处理器，该类用来注解装配bean。

## Import种类

我们知道在注解配置时，可以使用`@Import`注解引入其他的配置类。这里可以区分为3类：

### ImportSelector

该接口主要作用是收集需要导入的配置类，通过`selectImports`方法返回需要导入的配置类名称。如果该接口的实现类同时实现`EnvironmentAware`， `BeanFactoryAware` ，`BeanClassLoaderAware`或者`ResourceLoaderAware`，那么在调用其`selectImports`方法之前先调用上述接口中对应的方法，如果需要在所有的`@Configuration`处理完在导入时可以实现`DeferredImportSelector`接口。

#### AdviceModeImportSelector

这里需要提一下这个类。`@EnableAsync`这个注解，相比很熟悉了。在`@Component`类加上这个注解，我们就可以启用Spring为我们提供的`@Async`异步功能。我们可以看到在`@EnableAsync`上使用了`@Import(AsyncConfigurationSelector.class)`，该类继承自`AdviceModeImportSelector`类，会根据代理模式是`PROXY`还是`ASPECTJ`来导入不同的配置类，使用对应的方式来生成代理对象。前者会使用Jdk代理来实现。`AdviceModeImportSelector`类在导入之前会根据子类声明的注解来获取该注解，如果类上没有这个注解，则会报错。比如`AsyncConfigurationSelector`声明需要`@EnableAsync`注解，如果直接`@Import(AsyncConfigurationSelector)`而没有使用`@EnableAsync`注解时，会抛出异常，从而限制该配置类的使用范围，保证正确性。

### ImportBeanDefinitionRegistrar

该类用来通过编程方式注册Bean定义。

### 配置类

最后一种就是普通的配置类，包括上面提交的`@Configuration`等等，也可以直接引入一个普通类，该类也会被注册。

