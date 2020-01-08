package cn.sexycode.spring.study.chapter5;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * @author qzz
 * xml配置简单的aop示例
 */
public class XmlSimpleAopDemoApplication {
    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        xmlBeanDefinitionReader.loadBeanDefinitions("AopXmlSimpleConfig.xml");
        ((IBusinessService) beanFactory.getBean("businessProxy")).sayHello();
    }
}
