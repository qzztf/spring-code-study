package cn.sexycode.spring.study.chapter3;

import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author qzz
 */
public class MyNamespaceStartUpBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return MyBeanPostProcessor.class;
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
