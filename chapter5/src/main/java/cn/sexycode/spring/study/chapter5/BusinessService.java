package cn.sexycode.spring.study.chapter5;

public class BusinessService implements IBusinessService{
    @Override
    public String sayHello(){
        System.out.println("hello");
        return "hello";
    }
}
