package com.fanshuaiko.backage.entity.VO;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fanshuaiko.backage.utils.json.EmptyStringToIntegerDeserializer;
import com.fanshuaiko.backage.utils.json.EmptyStringToLongDeserializer;

import java.util.List;

/**
 * @ClassName TestVO
 * @Description
 * @Author fanshuaiko
 * @Date 19-3-27 下午5:18
 * @Version 1.0
 **/
public class TestVO {
    private Long id; //test主键

    private String name; //考试名称

    private Integer courseId; //所属课程id

    private String teacherNo; //任课教师id，也是创建考试的教师

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer totalScore; //考试总分

    private String status; //考试状态,0.未开始，1.进行中，3.已结束

    private String startTime; //开考时间

    private String endTime; //结束时间

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer testTime;//考试时长，单位分钟

    @JsonDeserialize(using = EmptyStringToLongDeserializer.class)
    private Long singleRedisId; //保存在redis中的单选题id

    @JsonDeserialize(using = EmptyStringToLongDeserializer.class)
    private Long judgeRedisId; //保存在redis中判断题id

    @JsonDeserialize(using = EmptyStringToLongDeserializer.class)
    private Long multipleRedisId; //保存在redis中多选题id

    @JsonDeserialize(using = EmptyStringToLongDeserializer.class)
    private Long subjectiveRedisId; //保存在redis中主观题id

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer singleScore; //单选题分值

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer judgeScore; //判断题分值

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer multipleScore; //多选题分值

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer subjectiveScore; //主观题分值

    private List<String> classNoList; //参与考试的班级

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer singleRandomCount; //选择题随机题数量

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer judgeRandomCount; //判断题随机题数量

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer multipleRandomCount; //多选题随机题数量

    @JsonDeserialize(using = EmptyStringToIntegerDeserializer.class)
    private Integer subjectiveRandomCount; //主观题随机题数量

    private String examNotice; //考试注意事项

    private String examIntro; //考试开头信息

    private Boolean cameraRequired; //是否要求摄像头/AI监控

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getTestTime() {
        return testTime;
    }

    public void setTestTime(Integer testTime) {

        this.testTime = testTime;
    }

    public Long getSingleRedisId() {
        return singleRedisId;
    }

    public void setSingleRedisId(Long singleRedisId) {
        this.singleRedisId = singleRedisId;
    }

    public Long getJudgeRedisId() {
        return judgeRedisId;
    }

    public void setJudgeRedisId(Long judgeRedisId) {
        this.judgeRedisId = judgeRedisId;
    }

    public Long getMultipleRedisId() {
        return multipleRedisId;
    }

    public void setMultipleRedisId(Long multipleRedisId) {
        this.multipleRedisId = multipleRedisId;
    }

    public Long getSubjectiveRedisId() {
        return subjectiveRedisId;
    }

    public void setSubjectiveRedisId(Long subjectiveRedisId) {
        this.subjectiveRedisId = subjectiveRedisId;
    }

    public Integer getSingleScore() {
        return singleScore;
    }

    public void setSingleScore(Integer singleScore) {
        this.singleScore = singleScore;
    }

    public Integer getJudgeScore() {
        return judgeScore;
    }

    public void setJudgeScore(Integer judgeScore) {
        this.judgeScore = judgeScore;
    }

    public Integer getMultipleScore() {
        return multipleScore;
    }

    public void setMultipleScore(Integer multipleScore) {
        this.multipleScore = multipleScore;
    }

    public Integer getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(Integer subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public List<String> getClassNoList() {
        return classNoList;
    }

    public void setClassNoList(List<String> classNoList) {
        this.classNoList = classNoList;
    }

    public Integer getSingleRandomCount() {
        return singleRandomCount;
    }

    public void setSingleRandomCount(Integer singleRandomCount) {
        this.singleRandomCount = singleRandomCount;
    }

    public Integer getJudgeRandomCount() {
        return judgeRandomCount;
    }

    public void setJudgeRandomCount(Integer judgeRandomCount) {
        this.judgeRandomCount = judgeRandomCount;
    }

    public Integer getMultipleRandomCount() {
        return multipleRandomCount;
    }

    public void setMultipleRandomCount(Integer multipleRandomCount) {
        this.multipleRandomCount = multipleRandomCount;
    }

    public Integer getSubjectiveRandomCount() {
        return subjectiveRandomCount;
    }

    public void setSubjectiveRandomCount(Integer subjectiveRandomCount) {
        this.subjectiveRandomCount = subjectiveRandomCount;
    }

    public String getExamNotice() {
        return examNotice;
    }

    public void setExamNotice(String examNotice) {
        this.examNotice = examNotice;
    }

    public String getExamIntro() {
        return examIntro;
    }

    public void setExamIntro(String examIntro) {
        this.examIntro = examIntro;
    }

    public Boolean getCameraRequired() {
        return cameraRequired;
    }

    public void setCameraRequired(Boolean cameraRequired) {
        this.cameraRequired = cameraRequired;
    }

    @Override
    public String toString() {
        return "TestVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseId=" + courseId +
                ", teacherNo='" + teacherNo + '\'' +
                ", totalScore=" + totalScore +
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", testTime=" + testTime +
                ", singleRedisId=" + singleRedisId +
                ", judgeRedisId=" + judgeRedisId +
                ", multipleRedisId=" + multipleRedisId +
                ", subjectiveRedisId=" + subjectiveRedisId +
                ", singleScore=" + singleScore +
                ", judgeScore=" + judgeScore +
                ", multipleScore=" + multipleScore +
                ", subjectiveScore=" + subjectiveScore +
                ", classNoList=" + classNoList +
                ", singleRandomCount=" + singleRandomCount +
                ", judgeRandomCount=" + judgeRandomCount +
                ", multipleRandomCount=" + multipleRandomCount +
                ", subjectiveRandomCount=" + subjectiveRandomCount +
                ", examNotice='" + examNotice + '\'' +
                ", examIntro='" + examIntro + '\'' +
                ", cameraRequired=" + cameraRequired +
                '}';
    }
}
