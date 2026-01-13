package com.fanshuaiko.backage.entity.VO;

import com.fanshuaiko.backage.entity.Test;

import java.util.List;

/** 考试预览 VO：包含考试基本信息、班级列表、题目列表 */
public class TestPreviewVo {
    private Test testInfo;              // 考试基本信息
    private List<String> classNoList;   // 参与考试的班级编号列表
    private List<QuestionReturnVo> questions; // 题目明细

    public Test getTestInfo() {
        return testInfo;
    }

    public void setTestInfo(Test testInfo) {
        this.testInfo = testInfo;
    }

    public List<String> getClassNoList() {
        return classNoList;
    }

    public void setClassNoList(List<String> classNoList) {
        this.classNoList = classNoList;
    }

    public List<QuestionReturnVo> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionReturnVo> questions) {
        this.questions = questions;
    }
}
