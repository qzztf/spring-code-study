package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

import java.util.Arrays;

/**
 * 测试构造函数标签
 * @author qzz
 */
public class ContextComponentScanNamespaceHandlerDemo {
    public static void main(String[] args) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions("ContextComponentScanNamespaceHandlerDemo.xml");
        System.out.println(Arrays.toString(factory.getBeanDefinitionNames()));
        System.out.println(((SimpleAdvice) factory.getBean("simpleAdvice")).getCom());
    }
}
