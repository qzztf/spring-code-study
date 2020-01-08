# Spring AOP 用法

前面讲到了AOP相关的思想和基本概念，实现方式可分为静态编绎型和代理两种方式。前者的代表作是`AspectJ`，在编绎阶段将通知织入到class中，需要用到特殊的工具来编绎。`AspectJ`定义了一种表达式语言来定义连接点，Spring 默认是基于JDK动态代理来实现AOP，并且只支持方法作为切入点。

## 如何使用

Spring 自己实现了一套AOP，使用起来不是太方便。还支持`AspectJ`，不过不完全支持。两种方式都可以使用编程式和xml配置方式。

使用Spring AOP大概有以下几种方式：

1. 在xml中配置`ProxyFactoryBean`，显式地设置advisors, advice, target等，或以编程方式使用`ProxyFactory`。Spring支持通过 jdk 动态代理和 cglib 来生成代理对象。前者只支持接口，后者可以支持类。
2. 配置`AutoProxyCreator`，这种方式下，还是如以前一样使用定义的bean，但是从容器中获得的其实已经是代理对象
3. 通过`<aop:config>`来配置，使用`AspectJ`的语法来定义切入点
4. 通过`<aop:aspectj-autoproxy>`来配置，使用`AspectJ`的注解来标识通知及切入点

### xml 配置 ProxyFactoryBean

1. 创建通知

   这里以前置通知类型为例。

   ```java
   public class UserBeforeAdvice implements MethodBeforeAdvice {
       @Override
       public void before(Method method, Object[] args, Object target) throws Throwable {
           System.out.println("Before method advice");
       }
   }
   ```

2. 创建业务代码

   ```java
   //接口
   public interface IBusinessService {
       String sayHello();
   }
   
   //实现类
   public class BusinessService implements IBusinessService{
       @Override
       public String sayHello(){
           System.out.println("hello");
           return "hello";
       }
   }
   ```

3. 配置代理类

   ```xml
   <!--业务实现-->
   <bean class="cn.sexycode.spring.study.chapter5.BusinessService" id="businessService"/>
   <!--通知实现-->
   <bean class="cn.sexycode.spring.study.chapter5.BusinessBeforeAdvice" id="userBeforeAdvice"/>
   <bean class="org.springframework.aop.framework.ProxyFactoryBean" id="businessProxy">
       <!--代理的接口-->
       <property name="interfaces" value="cn.sexycode.spring.study.chapter5.IBusinessService"/>
       <!--目标对象-->
       <property name="target" ref="businessService"/>
       <!--要应用的通知实现-->
       <property name="interceptorNames" value="userBeforeAdvice"/>
   </bean>
   ```

   经过上面的配置，已经将通知织入到代理对象中了，下面直接获取生成的代理对象，再调用方法即可以看到织入的结果。

4. 获取代理对象

   ```java
   DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
   XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
   xmlBeanDefinitionReader.loadBeanDefinitions("AopXmlSimpleConfig.xml");
   //获取代理对象
   ((IBusinessService) beanFactory.getBean("businessProxy")).sayHello();
   ```

   这里要注意的是要直接获取代理对象，然后转成我们的接口类型，再调用方法即可。

   输出结果：

   ```
   //打印的是通知的内容
   Before method advice
   //业务代码
   hello
   ```

   

### 实现方式

这种方式其实跟《AOP的基本概念》一文开头描述的思想类似，为每个业务实现类创建代理对象，只不过这里的织入时机已经可以配置了。

那么Spring 是如何生成代理对象的？又是如何织入通知的？

可以想象一下，在获取bean的时候，初始化所有的通知，并创建代理类，在调用代理对象的方法时调用通知的代码和原对象的方法。

`ProxyFactoryBean`类实现了`FactoryBean`接口。也就是说最终会通过`getObject`方法返回生成的对象。

看一下该类的类图：

![ProxyFactoryBean](Spring AOP基本用法/ProxyFactoryBean.png)

#### getObject 方法

前面讲到了，当Spring初始化此bean时，最终会调用`getObject`方法返回实际的bean。

```java
public Object getObject() throws BeansException {
		initializeAdvisorChain();
		if (isSingleton()) {
			return getSingletonInstance();
		}
		else {
			if (this.targetName == null) {
				logger.info("Using non-singleton proxies with singleton targets is often undesirable. " +
						"Enable prototype proxies by setting the 'targetName' property.");
			}
			return newPrototypeInstance();
		}
	}
```

第一步就是初始化切面链，配置此代理时，可以应用多个切面，所以最终会形成一个调用链。如果此bean是单例的，