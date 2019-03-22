package cn.sexycode.spring.study.chapter2;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 *
 * @author qinzaizhen
 */
public class DemoBeanPostProcessor implements BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("DemoBeanPostProcessor ---> postProcessBeforeInitialization");
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("DemoBeanPostProcessor ---> postProcessAfterInitialization");
        return bean;
    }


}
