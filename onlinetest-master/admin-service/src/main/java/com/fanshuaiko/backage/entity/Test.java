package com.fanshuaiko.backage.entity;

import java.util.Date;
/**
 * @ClassName Test
 * @Description 考试实体类
 * @Author fanshuaiko
 * @Date 19-3-15 上午10:57
 * @Version 1.0
 **/
public class Test {
    private Long id; //主键

    private String name; //考试名称

    private Integer courseId; //所属课程id

    private String teacherNo; //任课教师id，也是创建考试的教师

    private String totalScore; //考试总分

    private String status; //考试状态,0.未开始，1.进行中，3.已结束

    private Date startTime; //开考时间

    private Date endTime; //结束时间

    private Integer testTime; //test duration (minutes)

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
        this.name = name == null ? null : name.trim();
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
        this.teacherNo = teacherNo == null ? null : teacherNo.trim();
    }

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore == null ? null : totalScore.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getTestTime() {
        return testTime;
    }

    public void setTestTime(Integer testTime) {
        this.testTime = testTime;
    }

    public String getExamNotice() {
        return examNotice;
    }

    public void setExamNotice(String examNotice) {
        this.examNotice = examNotice == null ? null : examNotice.trim();
    }

    public String getExamIntro() {
        return examIntro;
    }

    public void setExamIntro(String examIntro) {
        this.examIntro = examIntro == null ? null : examIntro.trim();
    }

    public Boolean getCameraRequired() {
        return cameraRequired;
    }

    public void setCameraRequired(Boolean cameraRequired) {
        this.cameraRequired = cameraRequired;
    }

    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseId=" + courseId +
                ", teacherNo='" + teacherNo + '\'' +
                ", totalScore='" + totalScore + '\'' +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", testTime=" + testTime +
                ", examNotice='" + examNotice + '\'' +
                ", examIntro='" + examIntro + '\'' +
                ", cameraRequired=" + cameraRequired +
                '}';
    }
}
