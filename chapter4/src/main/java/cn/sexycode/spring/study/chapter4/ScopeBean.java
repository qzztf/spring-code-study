package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.factory.annotation.Value;

public class ScopeBean {
    @Value("#{myKey.first}")
    private String firstOfMyPair;

    public String getFirstOfMyPair() {
        return firstOfMyPair;
    }

    public void setFirstOfMyPair(String firstOfMyPair) {
        this.firstOfMyPair = firstOfMyPair;
    }
}
