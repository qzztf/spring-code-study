package cn.sexycode.spring.study.chapter4;

import org.springframework.core.convert.converter.Converter;

/**
 * @author qzz
 */
public class StringToIntegerConverter implements Converter<String, Integer> {
    @Override
    public Integer convert(String source) {
        System.out.println("使用 StringToIntegerConverter");
        return Integer.parseInt(source);
    }
}
