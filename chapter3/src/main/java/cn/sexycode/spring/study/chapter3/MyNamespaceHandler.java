package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 自定义命名空间解析
 * @author qzz
 */
public class MyNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("start-up",new MyNamespaceStartUpBeanDefinitionParser());
    }
}
