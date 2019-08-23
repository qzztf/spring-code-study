package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Arrays;

/**
 * BeanWrapperImpl 展示
 */
public class BeanWrapperDemo {
    public static void main(String[] args) {
        Student student = new Student();
        ClassRoom classRoom = new ClassRoom();
        classRoom.setName("room1");
        student.setClassRoom(classRoom);
        BeanWrapper wrapper = new BeanWrapperImpl(student);

        System.out.println("展示bean 的属性");
        Arrays.stream(wrapper.getPropertyDescriptors()).forEach(System.out::println);

        System.out.println("展示bean 的嵌套属性");
        wrapper = new PowerfulBeanWrapper(student);
        Arrays.stream(wrapper.getPropertyDescriptors()).forEach(System.out::println);

        System.out.println(wrapper.getPropertyValue("name"));
        System.out.println(wrapper.getPropertyValue("classRoom.name"));

        student = new Student();
        wrapper = new BeanWrapperImpl(student);
        wrapper.setAutoGrowNestedPaths(true);
        System.out.println("嵌套对象为空时：" + wrapper.getPropertyValue("classRoom.name"));
        student = new Student();
        wrapper = new BeanWrapperImpl(student);
        wrapper.setAutoGrowNestedPaths(true);
        wrapper.setPropertyValue("age",1);
        wrapper.setPropertyValue("classRoom.name", "room2");
        wrapper.setPropertyValue("classRoom.size", "2");
        System.out.println("设置属性类型不一致：" + wrapper.getPropertyValue("age"));
        System.out.println("设置嵌套对象的属性：" + wrapper.getPropertyValue("classRoom.name"));
        System.out.println("设置嵌套对象的属性：" + wrapper.getPropertyValue("classRoom.size"));

        System.out.println("自定义 PropertyEditor---------------------");
        student = new Student();
        wrapper = new BeanWrapperImpl(student);
        //注解自定义PropertyEditor
        wrapper.registerCustomEditor(ClassRoom.class, new ClassRoomPropertyEditor());
        wrapper.setPropertyValue("classRoom", "room3,3");
        System.out.println("自定义PropertyEditor, 设置嵌套对象的属性 name：" + wrapper.getPropertyValue("classRoom.name"));
        System.out.println("自定义PropertyEditor, 设置嵌套对象的属性 size：" + wrapper.getPropertyValue("classRoom.size"));
    }

}
