package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ScopeDemo {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext();
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        customScopeConfigurer.addScope(ThreadScope.SCOPE_NAME, new ThreadScope());
//        context.addBeanFactoryPostProcessor(customScopeConfigurer);
        context.addBeanFactoryPostProcessor(new ThreadScopeBeanFactoryPostProcessor());
        context.addBeanFactoryPostProcessor(new ScopeBeanDefinitionRegistryPostProcessor());
        context.refresh();
        System.out.println(context.getBean(ScopeBean.class).getFirstOfMyPair());
        System.out.println("同一线程：" + Thread.currentThread().getName() +":" + context.getBean(ScopeBean.class));
        System.out.println("同一线程：" + Thread.currentThread().getName() +":" + context.getBean(ScopeBean.class));
        new Thread(() -> {
            System.out.println("不同线程：" + Thread.currentThread().getName() +":" + context.getBean(ScopeBean.class));
        }).start();
        new Thread(() -> {
            System.out.println("不同线程：" + Thread.currentThread().getName() +":" + context.getBean(ScopeBean.class));
        }).start();
    }
}
