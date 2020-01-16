package cn.sexycode.spring.study.chapter5;

public class BusinessService implements IBusinessService{
    @Override
    public String sayHello(){
        System.out.println("hello");
        System.out.println("i want to say again");
        this.sayAgain();
        return "hello";
    }

    @Override
    public String sayAgain() {
        System.out.println("again");
        return "again";
    }
}
