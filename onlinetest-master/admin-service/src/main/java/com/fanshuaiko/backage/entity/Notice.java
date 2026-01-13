package com.fanshuaiko.backage.entity;

import java.io.Serializable;
import java.util.Date;

public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    /**
     * 0: 全部，1: 学生，2: 教师
     */
    private Integer targetRole;
    private String publisherNo;
    private String publisherName;
    private String publisherRoleName;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(Integer targetRole) {
        this.targetRole = targetRole;
    }

    public String getPublisherNo() {
        return publisherNo;
    }

    public void setPublisherNo(String publisherNo) {
        this.publisherNo = publisherNo;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherRoleName() {
        return publisherRoleName;
    }

    public void setPublisherRoleName(String publisherRoleName) {
        this.publisherRoleName = publisherRoleName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
