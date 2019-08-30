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

**注： 当嵌套对象为空时，默认获取嵌套对象的属性会抛出异常。**这时可以加一个设置：

> ```java
> wrapper.setAutoGrowNestedPaths(true);
> System.out.println("嵌套对象为空时：" + wrapper.getPropertyValue("classRoom.name"));
> ```

该属性的意义是自动扩展嵌套属性，按照默认值来初始化属性。此处就会将`classRoom`初始化，并且里面的属性为空。

```java
嵌套对象为空时：null
```

### 设置属性值

可以通过`setPropertyValue`方法来设置属性值。同上，当嵌套对象为空时，不能设置嵌套对象的属性，设置`wrapper.setAutoGrowNestedPaths(true)`即可。

注意以下代码：

```java
private String age;

wrapper.setPropertyValue("age",1);
```

在这里设置属性值的时候是整数型，但是`age`声明的时候是String。BeanWrapper是如何正确的赋值的呢？

`BeanWrapperImpl`内部会委托给`TypeConverterDelegate`类，先查找自定义`PropertyEditor`, 如果没有找到的话，则查找`ConversionService`，没有的话查找默认的`PropertyEditor`,再没有的话使用内部定义好的转换策略（按类型去判断，然后去转换）。

## PropertyEditor

`PropertyEditor`属于Java Bean规范里面的类，可以给GUI程序设置对象属性值提供方便，所以接口里有一些和GUI相关的方法，显然目前已经过时了。同时，官方文档上解释，它是线程不安全的。必须得有一个默认构造函数。可以想象一下，在界面上填入一个值，这个值一般来说都是`String`类型的，填入之后这个值能自动设置到对应的对象中（ 这里纯粹是我意淫的，对`awt`并不是很熟，不知道是不是这样）。了解安卓编程的朋友可能知道，我们要取界面上填的值，通常要拿到界面元素，然后再拿到值，然后再设置到对象中去。当界面上有很多个输入控件时，这样繁琐的操作，简直要人命。所以安卓后来出了数据绑定。这里有一篇[文章](https://www.iteye.com/blog/stamen-1525668)讲得很好。

BeanWrapperImpl内置了一些 `PropertyEditor`。

```java
private void createDefaultEditors() {
		this.defaultEditors = new HashMap<>(64);

		// Simple editors, without parameterization capabilities.
		// The JDK does not contain a default editor for any of these target types.
		this.defaultEditors.put(Charset.class, new CharsetEditor());
		this.defaultEditors.put(Class.class, new ClassEditor());
		this.defaultEditors.put(Class[].class, new ClassArrayEditor());
		this.defaultEditors.put(Currency.class, new CurrencyEditor());
		this.defaultEditors.put(File.class, new FileEditor());
		this.defaultEditors.put(InputStream.class, new InputStreamEditor());
		this.defaultEditors.put(InputSource.class, new InputSourceEditor());
		this.defaultEditors.put(Locale.class, new LocaleEditor());
		this.defaultEditors.put(Path.class, new PathEditor());
		this.defaultEditors.put(Pattern.class, new PatternEditor());
		this.defaultEditors.put(Properties.class, new PropertiesEditor());
		this.defaultEditors.put(Reader.class, new ReaderEditor());
		this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
		this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
		this.defaultEditors.put(URI.class, new URIEditor());
		this.defaultEditors.put(URL.class, new URLEditor());
		this.defaultEditors.put(UUID.class, new UUIDEditor());
		this.defaultEditors.put(ZoneId.class, new ZoneIdEditor());

		// Default instances of collection editors.
		// Can be overridden by registering custom instances of those as custom editors.
		this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
		this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
		this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
		this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
		this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

		// Default editors for primitive arrays.
		this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
		this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

		// The JDK does not contain a default editor for char!
		this.defaultEditors.put(char.class, new CharacterEditor(false));
		this.defaultEditors.put(Character.class, new CharacterEditor(true));

		// Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
		this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
		this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

		// The JDK does not contain default editors for number wrapper types!
		// Override JDK primitive number editors with our own CustomNumberEditor.
		this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
		this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
		this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
		this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
		this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
		this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
		this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
		this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
		this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
		this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
		this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
		this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
		this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
		this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

		// Only register config value editors if explicitly requested.
		if (this.configValueEditorsActive) {
			StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
			this.defaultEditors.put(String[].class, sae);
			this.defaultEditors.put(short[].class, sae);
			this.defaultEditors.put(int[].class, sae);
			this.defaultEditors.put(long[].class, sae);
		}
	}
```

这里没有注册`String`, 所以走的是内置方案，直接调用`toString`方法

```java
if (String.class == requiredType && ClassUtils.isPrimitiveOrWrapper(convertedValue.getClass())) {
					// We can stringify any primitive value...
					return (T) convertedValue.toString();
				}
```

### 自定义`PropertyEditor`

当Spring提供的`PropertyEditor`无法满足我们的需求时，我们可以自定义`PropertyEditor`。

一般不直接实现接口，而是继承`PropertyEditorSupport`类。Spring中大多数场景都是将传入的字符串转换成对应的属性值，需要重写`setAsText`方法。

```java
/**
 * 转换String -> ClassRoom;
 */
public class ClassRoomPropertyEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        //将逗号分隔的值转换成对象的属性值：room3,3
        String[] strings = Optional.ofNullable(text).orElseGet(String::new).split(",");
        ClassRoom classRoom = new ClassRoom();
        classRoom.setName(strings[0]);
        classRoom.setSize(Integer.parseInt(strings[1]));
        setValue(classRoom);
    }
}
```

上面的代码中，将字符串进行分隔，第一个值作为`ClassRoom`的`name`值，第二个值作为`size`。如何使用这个`PropertyEditor`?

先注册这个类，再设置`Student`的`classRoom`属性：

```java
wrapper = new BeanWrapperImpl(student);
//注解自定义PropertyEditor
wrapper.registerCustomEditor(ClassRoom.class, new ClassRoomPropertyEditor());
wrapper.setPropertyValue("classRoom", "room3,3");
```

这样就给`Student`类的`classRoom`属性进行了初始化。

## `ConversionService`

`ConversionService`是Spring提供的一套通用的类型转换机制的入口，相比`PropertyEditor`来说：

1. 支持的类型转换范围更广。
2. 支持从父类型转换为子类型，即多态。
3. 省去了Java GUI相关的概念。
4. 线程安全。

### 方法

- boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType)：如果可以将sourceType对象转换为targetType，则返回true。

  如果此方法返回true，则意味着`convert(Object, Class)`方法能够将`sourceType`实例转换为`targetType`。

  关于集合、数组和Map类型需要特别注意：**对于集合、数组和Map类型之间的转换，此方法将返回true，即使在底层元素不可转换的情况下，转换过程仍然可能生成一个`ConversionException`。在处理集合和映射时，调用者需要处理这种特殊情况。**

- boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType)：如果可以将sourceType对象转换为targetType，则返回true。`TypeDescriptor `提供关于将要发生转换的源和目标位置的附加上下文信息，通常是对象字段或属性的位置。

  如果此方法返回true，则意味着`convert(Object、TypeDescriptor、TypeDescriptor)`能够将`sourceType`实例转换为`targetType`。

  关于集合、数组和Map类型需要特别注意：**对于集合、数组和Map类型之间的转换，此方法将返回true，即使在底层元素不可转换的情况下，转换过程仍然可能生成一个`ConversionException`。在处理集合和映射时，调用者需要处理这种特殊情况。**

- <T> T convert(@Nullable Object source, Class<T> targetType)：将给定的对象转换为指定的`targetType`类型对象。

- Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType)：将给定的对象转换为指定的`targetType`类型对象。`TypeDescriptor `提供关于将要发生转换的源和目标位置的附加上下文信息，通常是对象字段或属性位置。

## `Converter`

`Converter`是具体的某类转换器接口，负责将某个类型的对象转成另外一个类型的对象。并且是一个函数式接口。

就提供了一个转换方法：

- T convert(S source)：转换对象类型。

## `ConverterRegistry`

用来注册`Converter`。

# `DirectFieldAccessor`