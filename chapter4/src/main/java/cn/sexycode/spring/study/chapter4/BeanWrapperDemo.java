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
    }

}
