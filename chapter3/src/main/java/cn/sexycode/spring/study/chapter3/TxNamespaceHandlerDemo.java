package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * 测试构造函数标签
 * @author qzz
 */
public class TxNamespaceHandlerDemo {
    public static void main(String[] args) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions("TxNamespaceHandlerDemo.xml");
        System.out.println(factory.getBean(Student.class));
    }
}
