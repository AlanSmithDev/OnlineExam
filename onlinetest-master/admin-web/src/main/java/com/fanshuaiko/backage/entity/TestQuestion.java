package com.fanshuaiko.backage.entity;

import java.util.Date;

/**
 * @ClassName TestQuestion
 * @Description
 * @Author fanshuaiko
 * @Date 19-4-1 上午10:05
 * @Version 1.0
 **/
public class TestQuestion {
    // 主键使用 Long 对应数据库 BIGINT
    private Long id;

    private Long testNo;
    private Long questionNo;
    private Integer score;
    private String questionType;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestNo() {
        return testNo;
    }

    public void setTestNo(Long testNo) {
        this.testNo = testNo;
    }

    public Long getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(Long questionNo) {
        this.questionNo = questionNo;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TestQuestion{" +
                "id=" + id +
                ", testNo=" + testNo +
                ", questionNo=" + questionNo +
                ", score=" + score +
                ", questionType='" + questionType + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
