package cn.sexycode.spring.study.chapter5.sameobject;

import cn.sexycode.spring.study.chapter5.IBusinessService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SameObjectDemo {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("sameobject.xml");
        applicationContext.getBean(IBusinessService.class).sayHello();
    }
}
