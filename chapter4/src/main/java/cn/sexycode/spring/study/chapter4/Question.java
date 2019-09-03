package cn.sexycode.spring.study.chapter4;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author qzz
 */
public class Question {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
