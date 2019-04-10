package cn.sexycode.spring.study.chapter3;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Date;

/**
 * ClassPathResource 示例
 * @author qinzaizhen
 */
public class DefaultResourceLoaderDemo {
    public static void main(String[] args) throws IOException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        //读取file文件
        System.out.println("------------------------------------读取本地文件------------------" );
        Resource resource = resourceLoader.getResource("file:///F:\\spring-code-study\\chapter3\\target\\classes/app.xml");
        System.out.println("资源文件是否存在：" + resource.exists());
        System.out.println("资源文件是否是文件：" + resource.isFile());
        System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
        System.out.println("资源文件名称：" + resource.getFilename());
        System.out.println("资源文件：" + resource.getFile());
        System.out.println("资源文件描述：" + resource.getDescription());
        System.out.println("资源文件URL：" + resource.getURL());
        System.out.println("资源文件URI：" + resource.getURI());
        System.out.println("资源文件长度：" + resource.contentLength());
        System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
        System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

        //读取jar包中的文件
        System.out.println("------------------------------------读取jar文件----------------------" );
        resource = resourceLoader.getResource("jar:file:///F:\\spring-code-study\\chapter3\\target\\chapter3-1.0-SNAPSHOT.jar!/app.xml");
        System.out.println("资源文件是否存在：" + resource.exists());
        System.out.println("资源文件是否是文件：" + resource.isFile());
        System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
        System.out.println("资源文件名称：" + resource.getFilename());
//        System.out.println("资源文件：" + resource.getFile());
        System.out.println("资源文件描述：" + resource.getDescription());
        System.out.println("资源文件URL：" + resource.getURL());
//        System.out.println("资源文件URI：" + resource.getURI());
        System.out.println("资源文件长度：" + resource.contentLength());
        System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
        System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

        //读取网络文件
        System.out.println("------------------------------------读取网络文件-----------------" );
        resource = resourceLoader.getResource("https://raw.githubusercontent.com/qzzsunly/spring-code-study/master/chapter3/src/main/resources/app.xml");
        System.out.println("资源文件是否存在：" + resource.exists());
        System.out.println("资源文件是否是文件：" + resource.isFile());
        System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
        System.out.println("资源文件名称：" + resource.getFilename());
//        System.out.println("资源文件：" + resource.getFile());
        System.out.println("资源文件描述：" + resource.getDescription());
        System.out.println("资源文件URL：" + resource.getURL());
//        System.out.println("资源文件URI：" + resource.getURI());
        System.out.println("资源文件长度：" + resource.contentLength());
        System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
        System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));


        //读取classpath下的文件
        System.out.println("------------------------------------读取classpath文件-----------------" );
        resource = resourceLoader.getResource("classpath:app.xml");
        System.out.println("资源文件是否存在：" + resource.exists());
        System.out.println("资源文件是否是文件：" + resource.isFile());
        System.out.println("资源文件是否可读：" + resource.isReadable());
//        System.out.println("资源文件是否可写：" + ((FileSystemResource)resource).isWritable());
        System.out.println("资源文件名称：" + resource.getFilename());
//        System.out.println("资源文件：" + resource.getFile());
        System.out.println("资源文件描述：" + resource.getDescription());
        System.out.println("资源文件URL：" + resource.getURL());
//        System.out.println("资源文件URI：" + resource.getURI());
        System.out.println("资源文件长度：" + resource.contentLength());
        System.out.println("资源文件最后修改时间：" + new Date(resource.lastModified()));
        System.out.println("资源文件数据：" + new String(resource.getInputStream().readAllBytes()));

    }
}
