package cn.sexycode.spring.study.chapter5;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public class XmlSimpleAopDemoApplication {
    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        xmlBeanDefinitionReader.loadBeanDefinitions("AopXmlSimpleConfig.xml");
        ((IBusinessService) beanFactory.getBean("businessProxy")).sayHello();
    }
}
