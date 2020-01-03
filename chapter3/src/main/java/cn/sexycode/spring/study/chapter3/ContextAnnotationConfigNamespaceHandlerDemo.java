package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 注解配置
 * @author qzz
 */
public class ContextAnnotationConfigNamespaceHandlerDemo {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext factory = new AnnotationConfigApplicationContext();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions("ContextAnnotationConfigNamespaceHandlerDemo.xml");
        factory.refresh();
        System.out.println(Arrays.toString(factory.getBeanDefinitionNames()));
    }

    public static class SimpleBean{
        private SeBean seBean;

        public SimpleBean() {
        }

        public SimpleBean(SeBean seBean) {
            this.seBean = seBean;
        }
    }
    public static class SeBean{

    }
    @Component
    @Import(SimpleBean.class)
    public static class Config{
        @Bean
        public static SimpleBean simpleBean(@Autowired SeBean seBean){
            return new SimpleBean(seBean);
        }

        @Bean
        public  SimpleBean simpleBean1(){
            return new SimpleBean(seBean());
        }

        @Bean
        public SeBean seBean(){
            System.out.println("aaaaa");
            return new SeBean();
        }
    }
}
