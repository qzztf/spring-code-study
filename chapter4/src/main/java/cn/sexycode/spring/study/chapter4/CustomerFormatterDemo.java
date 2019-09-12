package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.util.Date;

/**
 * @author qzz
 */
public class CustomerFormatterDemo {
    public static void main(String[] args) throws NoSuchFieldException {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addFormatter(new DateFormatter("yyyy-MM-dd"));
        Date date = new Date();
        System.out.println(conversionService.convert(date, String.class));

        conversionService = new DefaultFormattingConversionService();
        Question question = new Question();
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(question);
        beanWrapper.setConversionService(conversionService);
        beanWrapper.setPropertyValue("createTime", "2019-09-03");
        //再试试将Date 格式化成 字符串
        System.out.println(question.getCreateTime());
        System.out.println("注解格式化日期：" + conversionService.convert(question.getCreateTime(), new TypeDescriptor(question.getClass().getDeclaredField("createTime")), TypeDescriptor.valueOf(String.class)));
    }
}
