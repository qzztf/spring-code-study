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

这里需要提一下这个类。`@EnableAsync`这个注解，想必很熟悉了。在`@Component`类加上这个注解，我们就可以启用Spring为我们提供的`@Async`异步功能。我们可以看到在`@EnableAsync`上使用了`@Import(AsyncConfigurationSelector.class)`，该类继承自`AdviceModeImportSelector`类，会根据代理模式是`PROXY`还是`ASPECTJ`来导入不同的配置类，使用对应的方式来生成代理对象。前者会使用Jdk代理来实现。`AdviceModeImportSelector`类在导入之前会根据子类声明的注解来获取该注解，如果类上没有这个注解，则会报错。比如`AsyncConfigurationSelector`声明需要`@EnableAsync`注解，如果直接`@Import(AsyncConfigurationSelector)`而没有使用`@EnableAsync`注解时，会抛出异常，从而限制该配置类的使用范围，保证正确性。

### ImportBeanDefinitionRegistrar

该类用来通过编程方式注册Bean定义。

### 配置类

最后一种就是普通的配置类，包括上面提交的`@Configuration`等等，也可以直接引入一个普通类，该类也会被注册。

### `@Bean`注解

用该注解标注的方法代表着，该方法会返回一个返回值类型的Bean。方法可以是静态的，也可以是实例方法。最终在解析时，该方法会被解析成*工厂方法*，在实例化Bean时，以工厂方法的形式实例化Bean。

有一点要注意，如果不指定Bean的名字，那么默认将会以方法名作为Bean的名字。存在多个相同名称的方法时，将会出现Bean覆盖的情况。具体逻辑体现在这个方法中：`org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#isOverriddenByExistingDefinition`。当此方法返回`false`时，新的bean将会覆盖已存在的bean。返回`true`时，会忽略新的bean。

1. 当已存在的bean是配置类中定义的时，同一个配置类中定义的同名Bean(`ConfigurationClassBeanDefinition`)不允许覆盖，不同配置类中定义的同名Bean允许覆盖
2. 同名的Bean是注解扫描出来的（`ScannedGenericBeanDefinition`），允许覆盖
3. 高于应用级别的Bean（Role >`BeanDefinition.ROLE_APPLICATION`）允许覆盖应用级别的Bean
4. 最后判断`BeanDefinitionRegistry`是否是`DefaultListableBeanFactory`，并且`isAllowBeanDefinitionOverriding()`为`false`时，抛出异常。
5. 其余情况不允许覆盖，直接忽略新bean。

从最后一点可以看出，默认情况下XML注册的bean优先级比编程方式注册的bean优先级高。因为编程方式注册的bean是`ConfigurationClassBeanDefinition`，而xml注册的是`GenericBeanDefinition`，当xml先注册时，编程方式注册的bean满足情况5，会直接忽略。反过来时，满足情况1，会被xml配置覆盖。

# 方式对比

前三种方式，除了载体的区别外是一样的逻辑。这里只比较xml，注解，编码方式。

|                  | XML配置                                                      | 注解配置                                                     | Java编程方式                                                 |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Bean定义         | 通过`<bean>`标签                                             | 在实现类上加`@Component`注解，包括被`@Component`注解的注解，如：`@Service`、`@Repository`、`@Controller`等 | `@Configuration`或`@Component`注解的类中`@Bean`方法，在方法中提供实例化逻辑，并返回对象 |
| Bean名称         | 通过`<bean>`标签的`id`、`name`属性                           | 通过注解的`value`属性定义。默认为小写字母的类名（没有包名）  | 通过`@Bean`的`name`属性指定。默认为方法名                    |
| Bean装配         | 通过`<property>`子标签或者通过`p`命名空间。                  | 通过成员变量处加`@Autowired`或`@Resource`注解，配合`@Qualifier`限制名称。`@Primary`设置多个同类型的bean时优先使用。 | 在方法入参中使用`@Autowired`，然后代码设置。                 |
| Bean生命周期方法 | 通过`<bean>`的`init-method`和`destory-method`属性指定方法名，只有指定一个初始化和销毁方法 | 通过在方法上加`@PostConstruct`和`@PreDestory`注解，可以指定多个 | 通过`@Bean`的`initMethod`和`destoryMethod`方法指定。对于初始化方法，直接在方法内部通过代码编写初始化逻辑 |
| Bean作用范围     | 通过`<bean>`标签的`scope`属性指定                            | 在类上加`@Scope`注解                                         | 在方法上加`@Scope`注解                                       |
| Bean延迟初始化   | 通过`<bean>`的`lazy-init`属性指定，默认为`default`，继承自`<beans>`的`default-lazy-init`设置，该值默认为`false` | 在类上加`@Lazy`注解                                          | 在方法上加`@Lazy`注解                                        |
| 适用场景         | 1. Bean实现类来源于第三方库，如`DataSource`等             2.  命名空间的配置，如：`aop`, `context` | 当前项目的实现类                                             | 第三方库和本项目类均可。逻辑可控制，初始化逻辑复杂时优势更明显 |
| 优点             | 降低代码耦合，容易扩展。修改不需要重新编绎代码。可以清晰表达Bean之间的关系。 | 维护容易，代码量少。编绎期间即可以校验正确性。在类中体现出Bean之间的关系 | 可以精确控制Bean的初始化过程                                 |
| 缺点             | 查错只能在运行期，查错麻烦。配置与代码相对独立，导致改了一方会影响到另一方。配置文件多了之后难以维护 | 修改需要重新编绎代码                                         | 修改需要重新编绎代码                                         |