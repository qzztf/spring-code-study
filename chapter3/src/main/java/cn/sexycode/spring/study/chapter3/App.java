package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * Hello world!
 *
 * @author qinzaizhen
 */
public class App 
{
    public static void main( String[] args )
    {
        // 1. 初始化一个bean 工厂
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        // 2. 初始化XmlBeanDefinitionReader,负责从xml文件中读取bean定义
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        // 3. 加载bean 定义的入口方法
        reader.loadBeanDefinitions("classpath:app.xml");
    }
}
