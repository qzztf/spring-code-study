package cn.sexycode.spring.study.chapter3;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * ClassPathResource 示例
 * @author qinzaizhen
 */
public class ClassPathResourceDemo {
    public static void main(String[] args) throws IOException {
        ClassPathResource resource = new ClassPathResource("app.xml");
        System.out.println("资源文件是否存在：" + resource.exists());
        System.out.println("资源文件名称：" + resource.getFilename());
        System.out.println("资源文件：" + resource.getFile());
    }
}
