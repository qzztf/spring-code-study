package cn.sexycode.spring.study.chapter4;

/**
 * 工厂Bean
 */
public class FactoryBeanBean {
    public Bean getBean(){
        return new Bean();
    }
    public Bean getBean(String name){
        return new Bean();
    }
    public Bean getBean(Integer name){
        return new Bean();
    }
    public static class Bean{
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
