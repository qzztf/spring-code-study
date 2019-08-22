# BeanWrapper

## BeanWrapper是什么

Spring底层操作Java Bean的核心接口。

通常不直接使用该接口，而是通过`BeanFactory`或`DataBinder`。

提供分析和操作标准Java Bean的操作: 获取和设置属性值(单个或批量)、获取属性描述以及查询属性的可读性/可写性的能力。

此接口还支持嵌套属性，允许将子属性上的属性设置为无限深度。

`BeanWrapper`的`extractOldValueForEditor`默认值是`false`，以避免`getter`方法调用造成的副作用。将此选项变为`true`，以便向自定义编辑器暴露当前属性值。

可以看出`BeanWrapper`是操作Java Bean 的强大利器。

## 类结构

![BeanWrapper类结构](BeanWrapper/BeanWrapper类结构.png)

BeanWrapper 继承自`TypeConverter`，`PropertyEditorRegistry`，`PropertyAccessor`, `ConfigurablePropertyAccessor`接口。从名称可以看出具备了类型转换，属性编辑器注册，属性访问及配置的功能。

## 使用方式

接下来看看如何使用BeanWrapper来操作我们的Java Bean。

Spring给我们提供了一个实现类`BeanWrapperImpl`，我们就用这个类来展示。

### 获取属性

Bean对象：

```java
public class Student {
    private String name;

    private String age;

    private ClassRoom classRoom;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }
}
```

定义了*3*个属性，看一下使用方法：

```java
Student student = new Student();
BeanWrapper wrapper = new BeanWrapperImpl(student);
System.out.println("展示bean 的属性");
Arrays.stream(wrapper.getPropertyDescriptors()).forEach(System.out::println);
```

结果如下：

```java
展示bean 的属性
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=age]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=class]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=classRoom]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=name]
```

可以看出将有`get`方法的属性已经打印出来了。同时可以看到多打印了一个`class`属性，但是我们的类里面没有定义这个属性，`Object`类中有`getClass`的方法。我们大胆猜测Spring会遵循Java Bean的设计原则，通过`get`方法来获取属性。 

现在将`age`改成`age1`，`getAge`方法不变，看一下结果。

```java
展示bean 的属性
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=age]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=class]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=classRoom]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=name]
```

打印出来的属性名一样。现在交换一下，将`getAge`改成`getAge1`，属性`age1`改成`age`。

```java
展示bean 的属性
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=age1]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=class]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=classRoom]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=name]
```

可以看到获取到的属性已经变成了`age1`。这充分验证了我们的猜想。

我们可以看一下Spring的代码，里面使用了`java.beans`包下`Introspector`类来获取Bean的信息。

#### 嵌套属性

上面的结果中，我们并没有获取到ClassRoom的属性。BeanWrapper并不支持这种操作，我们可以扩展一下，比如判断属性，如果是自定义的类型，那么就再调用一次BeanWrapper的方法。这有个前提是这个属性不为`null`。

```java
Student student = new Student();
student.setClassRoom(new ClassRoom());
BeanWrapper wrapper = new BeanWrapperImpl(student);

System.out.println("展示bean 的属性");
Arrays.stream(wrapper.getPropertyDescriptors()).forEach(System.out::println);

System.out.println("展示bean 的嵌套属性");
wrapper = new PowerfulBeanWrapper(student);
Arrays.stream(wrapper.getPropertyDescriptors()).forEach(System.out::println);
```

先上结果：

```java
展示bean 的属性
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=age]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=class]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=classRoom]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=name]
展示bean 的嵌套属性
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=age]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=class]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=classRoom]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=name]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=class]
org.springframework.beans.GenericTypeAwarePropertyDescriptor[name=name]
```

ClassRoom类只有一个name 属性，这里看到也打印出来了。证明思路是对的，只是这个结构还需要组织一下，现在是扁平的。

下面看一下`PowerfulBeanWrapper`的实现方式

```java
public class PowerfulBeanWrapper extends BeanWrapperImpl {
    public PowerfulBeanWrapper(Object o) {
        super(o);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] propertyDescriptors = super.getPropertyDescriptors();
        List<PropertyDescriptor> propertyDescriptorList = new ArrayList<>(Arrays.asList(propertyDescriptors));
        Arrays.stream(propertyDescriptors).forEach(propertyDescriptor -> {
            Object value = getPropertyValue(propertyDescriptor.getName());
            if (value != null && !(value instanceof Class) && !value.getClass().isPrimitive()) {
                propertyDescriptorList.addAll(Arrays.asList(new BeanWrapperImpl(value).getPropertyDescriptors()));
            }
        });
        return propertyDescriptorList.toArray(new PropertyDescriptor[0]);
    }
}
```

直接继承自`BeanWrapperImpl`类，覆盖了`getPropertyDescriptors`方法。遍历属性值，如果不为空并且不是Class，则再获取一次这个属性的属性。这里只获取了2层属性，可以在获取嵌套属性时换成我们的`PowerfulBeanWrapper`类既可支持无限层。

### 获取属性值

可以使用BeanWrapper的`getPropertyValue`方法来获取属性值。上面的代码中已经展示过了。

支持获取嵌套属性：

```java
Student student = new Student();
ClassRoom classRoom = new ClassRoom();
classRoom.setName("room1");
student.setClassRoom(classRoom);
BeanWrapper wrapper = new BeanWrapperImpl(student);
System.out.println(wrapper.getPropertyValue("name"));
System.out.println(wrapper.getPropertyValue("classRoom.name"));
```

结果：

```java
null
room1
```

可以看出来还是很方便的。