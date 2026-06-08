package com.fanshuaiko.backage.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 即时通讯消息实体
 */
public class ImMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String senderNo;
    private String senderName;
    private String content;
    private String target;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderNo() {
        return senderNo;
    }

    public void setSenderNo(String senderNo) {
        this.senderNo = senderNo;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
