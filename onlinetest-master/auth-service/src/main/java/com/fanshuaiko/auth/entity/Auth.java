package com.fanshuaiko.auth.entity;

import java.io.Serializable;

/**
 * @ClassName Auth
 * @Description 登录认证实体类
 * @Author fanshuaiko
 * @Date 19-3-20 下午2:20
 * @Version 1.0
 **/
public class Auth implements Serializable {

    /** 主键（与库表 bigint 一致，12 位编号；必须用 Long 以便 MyBatis 走 BIGINT 映射） */
    private Long id;
    private String username;//学号或者教师编号
    private String password;//密码
    private String roleId;//角色id
    private String flag;//标志，1.学生，2.教师
    private String salt;//盐值

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roleId='" + roleId + '\'' +
                ", flag='" + flag + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
