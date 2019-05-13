package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Hello world!
 *
 * @author qinzaizhen
 */
public class MvcAnnotationDrivenDemo
{
    public static void main( String[] args )
    {
        XmlWebApplicationContext applicationContext = new XmlWebApplicationContext();
        applicationContext.setConfigLocation("WebMvcAnnotationDrivenNamespaceHandlerDemo.xml");
        applicationContext.refresh();
    }
}
