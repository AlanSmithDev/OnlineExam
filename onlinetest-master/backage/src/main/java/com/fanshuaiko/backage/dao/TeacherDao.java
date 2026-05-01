package com.fanshuaiko.backage.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.fanshuaiko.backage.entity.Teacher;

@Mapper
@Repository
public interface TeacherDao {
    Teacher selectByTeacherNo(@Param("teacherNo") String teacherNo);
    int updateAvatarByTeacherNo(@Param("teacherNo") String teacherNo, @Param("avatarBlob") byte[] avatarBlob, @Param("avatarName") String avatarName, @Param("avatarMime") String avatarMime);
}
