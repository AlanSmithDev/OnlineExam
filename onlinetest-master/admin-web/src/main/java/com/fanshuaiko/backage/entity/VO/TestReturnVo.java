package com.fanshuaiko.backage.entity.VO;

import java.util.List;

/**
 * @ClassName TestReturnVo
 * @Description 查询考试信息返回类
 * @Author fanshuaiko
 * @Date 19-3-29 下午2:21
 * @Version 1.0
 **/
public class TestReturnVo {
    private Long id; // test主键

    private String name; // 考试名称

    private Integer courseId; // 课程ID
    private String courseName; // 所属课程
    private String teacherNo; // 教师编号
    private String teacherName; // 任课教师，也是创建考试的教师
    private int totalScore; // 考试总分

    private String status; // 考试状态,0.未开始，1.进行中，3.已结束
    private String startTime; // 开考时间
    private String endTime; // 结束时间

    private int testTime; // 考试时长

    private List<Long> questionNo; // 本场考试题目id

    private List<String> classNo; // 本场考试班级id

    private String examNotice; // 考试注意事项

    private String examIntro; // 考试开头信息

    private Boolean cameraRequired; // 是否要求摄像头/AI监控

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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
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

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public List<Long> getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(List<Long> questionNo) {
        this.questionNo = questionNo;
    }

    public List<String> getClassNo() {
        return classNo;
    }

    public void setClassNo(List<String> classNo) {
        this.classNo = classNo;
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
        return "TestReturnVo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", teacherNo='" + teacherNo + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", totalScore=" + totalScore +
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", testTime=" + testTime +
                ", questionNo=" + questionNo +
                ", classNo=" + classNo +
                ", examNotice='" + examNotice + '\'' +
                ", examIntro='" + examIntro + '\'' +
                ", cameraRequired=" + cameraRequired +
                '}';
    }
}
