package cn.sexycode.spring.study.chapter4;

import java.util.List;

public class StringFormatEntity {
    @StringFormat(pattern = "\\d+")
    private List<Integer> formats;
}
