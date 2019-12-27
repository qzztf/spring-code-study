package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 注解配置
 * @author qzz
 */
public class ContextAnnotationConfigNamespaceHandlerDemo {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext factory = new AnnotationConfigApplicationContext();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions("ContextAnnotationConfigNamespaceHandlerDemo.xml");
        factory.refresh();
        System.out.println(Arrays.toString(factory.getBeanDefinitionNames()));
    }

    public static class SimpleBean{

    }
    @Configuration
    public static class Config{
        @Bean
        public SimpleBean simpleBean(){
            return new SimpleBean();
        }
    }
}
