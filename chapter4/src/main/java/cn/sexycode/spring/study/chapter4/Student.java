package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.factory.annotation.Value;

/**
 *
 */
public class Student {
    @Value("${name}")
    private String name;
    private String age;

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

}
