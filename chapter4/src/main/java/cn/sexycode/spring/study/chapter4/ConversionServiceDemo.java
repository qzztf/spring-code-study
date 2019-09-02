package cn.sexycode.spring.study.chapter4;

import org.springframework.core.convert.support.DefaultConversionService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConversionServiceDemo {
    public static void main(String[] args) {
        DefaultConversionService conversionService = new DefaultConversionService();
        List source = new ArrayList();
        source.add("1");
        source.add(2);
        List<Long> longList = new ArrayList<>(){};
        for (Object s : source) {
            System.out.println(s.getClass() + " , " + s);
        }
        List convert = conversionService.convert(source, longList.getClass());
        for (Object s : convert) {
            System.out.println(s.getClass() + " , " + s);
        }
    }
}
