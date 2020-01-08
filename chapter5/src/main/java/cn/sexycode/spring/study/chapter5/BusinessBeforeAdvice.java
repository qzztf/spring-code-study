package cn.sexycode.spring.study.chapter5;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class BusinessBeforeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("Before method advice");
    }
}