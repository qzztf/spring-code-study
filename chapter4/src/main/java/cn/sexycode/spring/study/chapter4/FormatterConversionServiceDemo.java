package cn.sexycode.spring.study.chapter4;

import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.util.Date;

public class FormatterConversionServiceDemo {
    public static void main(String[] args) {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addFormatter(new DateFormatter("yyyy-MM-dd"));
        Date date = new Date();
        System.out.println(conversionService.convert(date, String.class));
    }
}
