# Spring 配置方式

从Spring诞生开始，用的是XML配置形式，这在那个年代是常用的方式。修改XML不需要重新编绎class文件，但是由于没有校验机制，配置正确于否，只能在程序运行的时候才能知晓。灵活性也欠佳，无法实现复杂逻辑。当Java 5推出注解之后，Spring 又推出了基于注解的配置方式，可以达到在编绎期就能判断配置是否正确的目的，但是修改注解之后，需要重新编绎class。又在注解的基础上推出了编程式的配置方式，将灵活性推到了另高的层次。Spring还有两种用得较少的配置方式：一是`properties`文件，这种单行文件配置起来异常复杂，也不方便理解，实际应用不多。二是groovy脚本的形式，这种形式也可以非常灵活并且很简洁，不过熟悉java语法的朋友可能还需要熟悉一下。

## XML形式

这种形式在配置文件的解析一文已写，通过`XmlBeanDefinitionReader` 类来读取xml配置文件并解析。

# Properties形式

通过`PropertiesBeanDefinitionReader`来读取配置文件并解析。

# Groovy 脚本形式

通过`GroovyBeanDefinitionReader`来读取配置文件并解析。

# 注解形式

之前在《bean定义文件解析》一文中提到`ComponentScanBeanDefinitionParser`用来解析`<context:component-scan/>`标签，在解析的过程中会根据规则如注解，`package`，`type`等来扫描对应的bean class。但是这个注解无法对对象属性进行装配，需要结合`<context:annotation-config/>`标签来实现完整的注解配置。

# Java 编程形式

之前在《bean定义文件解析》一文中提到`AnnotationConfigBeanDefinitionParser`用来解析`<context:annotation-config/>`标签，在解析的过程中会注册`ConfigurationClassPostProcessor`，此类实现`BeanDefinitionRegistryPostProcessor`接口，可以在初始化bean之前修改bean定义，在该类中会注册。在此标签的解析过程中还会注册`AutowiredAnnotationBeanPostProcessor`后处理器，该类用来注解装配bean。