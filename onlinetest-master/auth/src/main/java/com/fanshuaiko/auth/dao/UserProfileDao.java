package com.fanshuaiko.auth.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileDao {

    @Select("select count(1) from teacher where teacher_no = #{no}")
    int countTeacher(@Param("no") String teacherNo);

    @Select("select count(1) from student where student_no = #{no}")
    int countStudent(@Param("no") String studentNo);

    // Fallback fuzzy/prefix counts used when exact match fails (compatibility with different id formats)
    @Select("select count(1) from teacher where teacher_no like concat(#{prefix}, '%')")
    int countTeacherLike(@Param("prefix") String prefix);

    @Select("select count(1) from student where student_no like concat(#{prefix}, '%')")
    int countStudentLike(@Param("prefix") String prefix);
}
