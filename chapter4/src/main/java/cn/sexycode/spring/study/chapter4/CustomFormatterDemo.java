package cn.sexycode.spring.study.chapter4;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;

/**
 * @author qzz
 */
public class CustomFormatterDemo {
    public static void main(String[] args) throws NoSuchFieldException {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addFormatterForFieldAnnotation(new StringFormatAnnotationFormatterFactory());
        StringFormatEntity formatEntity = new StringFormatEntity();
        System.out.println("自定义注解格式化：" + conversionService.convert("fff43ffd344", TypeDescriptor.valueOf(String.class) , new TypeDescriptor(formatEntity.getClass().getDeclaredField("formats"))));
    }
}
