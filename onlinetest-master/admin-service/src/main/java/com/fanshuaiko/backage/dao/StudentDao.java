package com.fanshuaiko.backage.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.fanshuaiko.backage.entity.Student;

@Mapper
@Repository
public interface StudentDao {
    Student selectByStudentNo(@Param("studentNo") String studentNo);
    int updateAvatarByStudentNo(@Param("studentNo") String studentNo, @Param("avatarBlob") byte[] avatarBlob, @Param("avatarName") String avatarName, @Param("avatarMime") String avatarMime);
    int updateProfileByStudentNo(@Param("studentNo") String studentNo,
                                 @Param("name") String name,
                                 @Param("classNo") String classNo,
                                 @Param("className") String className,
                                 @Param("phone") String phone,
                                 @Param("email") String email);
}
