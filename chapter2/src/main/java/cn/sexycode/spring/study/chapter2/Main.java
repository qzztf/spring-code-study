package cn.sexycode.spring.study.chapter2;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * @author qinzaizhen
 */
public class Main {

    public static void main(String[] args) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions(new ClassPathResource("app.xml"));
        factory.getBean(DemoBeanFactoryPostProcessor.class);
//        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("app.xml");
    }
}
