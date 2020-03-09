package cn.sexycode.spring.study.chapter5;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * ProxyFactory 使用示例
 *
 * @author qzzsu
 */
public class ProxyFactoryDemo {
    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        xmlBeanDefinitionReader.loadBeanDefinitions("AopXmlSimpleConfig.xml");
        IBusinessService bean = beanFactory.getBean("businessService", IBusinessService.class);
        Advice userBeforeAdvice = beanFactory.getBean("userBeforeAdvice", Advice.class);
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.addAdvice(userBeforeAdvice);
        IBusinessService proxy = (IBusinessService) proxyFactory.getProxy();
        proxy.sayAgain();
    }
}
