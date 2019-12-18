package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 工厂Bean
 */
public class FactoryBeanBean {
    public Bean getBean(){
        return new Bean();
    }
    public Bean getBean(Long name){
        return new Bean();
    }
    public Bean getBean(Integer name){
        return new Bean();
    }
    public static class Bean{
        public Bean() {
        }

        public Bean(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public static class ConstructorBean{
        public ConstructorBean() {
        }
        @Autowired
        public ConstructorBean(String name) {
            this.name = name;
        }

//        @Autowired
        public ConstructorBean(Integer name) {
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
