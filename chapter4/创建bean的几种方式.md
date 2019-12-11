# Spring 创建Bean的方式

在Spring创建Bean时，会遵循下面的顺序来创建。这个顺序是在`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])`方法的内在逻辑。

1. `InstantiationAwareBeanPostProcessor`
2. `InstanceSupplier`
3. factory method
4. constructor autowiring
5. simple instantiation

## `InstantiationAwareBeanPostProcessor`

`InstantiationAwareBeanPostProcessor`接口是Spring内部用来