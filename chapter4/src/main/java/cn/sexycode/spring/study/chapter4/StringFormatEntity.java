package cn.sexycode.spring.study.chapter4;

import java.util.ArrayList;
import java.util.List;

public class StringFormatEntity {
    @StringFormat(pattern = "\\d+")
    private List<String> formats = new ArrayList<>();

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
}
