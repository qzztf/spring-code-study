package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ScopeDemo {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext();
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        customScopeConfigurer.addScope(ThreadScope.SCOPE_NAME, new ThreadScope());
        context.addBeanFactoryPostProcessor(customScopeConfigurer);
        context.addBeanFactoryPostProcessor(new ThreadScopeBeanFactoryPostProcessor());
        context.addBeanFactoryPostProcessor(new ScopeBeanDefinitionRegistryPostProcessor());
        context.refresh();
        context.getBean(ScopeBean.class);
    }
}
