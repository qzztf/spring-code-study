package cn.sexycode.spring.study.chapter6.propagation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author qzz
 */
@SpringBootApplication
@EnableTransactionManagement
public class PropagationApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PropagationApp.class, args);
        OuterService bean = context.getBean(OuterService.class);
        bean.outer();
    }
}
