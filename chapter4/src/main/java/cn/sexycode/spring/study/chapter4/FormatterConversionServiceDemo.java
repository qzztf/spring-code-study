package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.util.Date;

/**
 * @author qzz
 */
public class FormatterConversionServiceDemo {
    public static void main(String[] args) {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addFormatter(new DateFormatter("yyyy-MM-dd"));
        Date date = new Date();
        System.out.println(conversionService.convert(date, String.class));

        conversionService = new DefaultFormattingConversionService();
        Question question = new Question();
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(question);
        beanWrapper.setConversionService(conversionService);
        beanWrapper.setPropertyValue("createTime", "2019-09-03");
        System.out.println(question.getCreateTime());
    }
}
