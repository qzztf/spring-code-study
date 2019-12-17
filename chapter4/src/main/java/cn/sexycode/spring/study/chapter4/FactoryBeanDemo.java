package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * 工厂方法初始化
 */
public class FactoryBeanDemo {
    public static void main(String[] args) {

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions("classpath:factory.xml");
        System.out.println(factory.getBean("bean"));
    }
}
