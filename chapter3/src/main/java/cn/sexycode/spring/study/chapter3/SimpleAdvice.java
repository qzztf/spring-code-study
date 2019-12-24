package cn.sexycode.spring.study.chapter3;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author qinzaizhen
 */
@Component
public class SimpleAdvice implements BeforeAdvice {

    @Autowired
    private Com com;

    public Com getCom() {
        return com;
    }

    public void setCom(Com com) {
        this.com = com;
    }

    @Component
    public static class Com{

    }

}
