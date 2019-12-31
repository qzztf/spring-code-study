# Spring 配置方式

从Spring诞生开始，用的是XML配置形式，这在那个年代是常用的方式。修改XML不需要重新编绎class文件，但是由于没有校验机制，配置正确于否，只能在程序运行的时候才能知晓。灵活性也欠佳，无法实现复杂逻辑。当Java 5推出注解之后，Spring 又推出了基于注解的配置方式，可以达到在编绎期就能判断配置是否正确的目的，但是修改注解之后，需要重新编绎class。又在注解的基础上推出了编程式的配置方式，将灵活性推到了另高的层次。Spring还有两种用得较少的配置方式：一是`properties`文件，这种单行文件配置起来异常复杂，也不方便理解，实际应用不多。二是groovy脚本的形式，这种形式也可以非常灵活并且很简洁，不过熟悉java语法的朋友可能还需要熟悉一下。

## XML形式

这种形式在配置文件的解析一文已写，通过`XmlBeanDefinitionReader` 类来读取xml配置文件并解析。

# Properties形式

通过`PropertiesBeanDefinitionReader`来读取配置文件并解析。

# Groovy 脚本形式

通过`GroovyBeanDefinitionReader`来读取配置文件并解析。

# 注解形式

之前在《bean定义文件解析》一文中提到`ComponentScanBeanDefinitionParser`用来解析`<context:component-scan/>`标签，在解析的过程中会根据规则如注解，`package`，`type`等来扫描对应的注解class，可以设置多个package，用`,; \t\n`进行分割。默认会扫描`@Component`注解的类，也可以配置自己的类型过滤器。但是这个注解无法对对象属性进行装配，需要结合`<context:annotation-config/>`标签来实现完整的注解配置。从Spring 5 开始默认扫描`@Indexed`注解，`@Component`也加上了该注解，因此也会扫描到。

# Java 编程形式

之前在《bean定义文件解析》一文中提到`AnnotationConfigBeanDefinitionParser`用来解析`<context:annotation-config/>`标签，在解析的过程中会注册`ConfigurationClassPostProcessor`，此类实现`BeanDefinitionRegistryPostProcessor`接口，可以在初始化bean之前修改bean定义。

1. 在注解扫描（实际上是已被注册的bean定义）的基础上去筛选`@Configuration`配置类以及轻量级配置类`@Component`、`@ComponentScan` 、`@Import`、`@ImportResource`，以及方法级注解`@Bean`。这里有一点要提一下：`@Configuration`注解也被``@Component`注解修饰，换句话说*被`@Configuration`注解的类也会注册为bean*。

2. 筛选出上述配置类后，接下来需要解析这些类。具体会交给`org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitions`方法。在此方法会先校验是否需要跳过加载该配置类（这里包括了上述的轻量级配置类），主要通过`@Conditional`注解（4.0新增，Spring Boot用来实现条件注册的核心）来实现，如果需要跳过，则将此Bean定义移除。然后解析该类上的`@Import`注解。

在此标签的解析过程中还会注册`AutowiredAnnotationBeanPostProcessor`后处理器，该类用来注解装配bean。