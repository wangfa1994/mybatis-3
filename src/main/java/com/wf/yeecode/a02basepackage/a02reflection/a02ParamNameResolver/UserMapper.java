package com.wf.yeecode.a02basepackage.a02reflection.a02ParamNameResolver;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    List<User> queryUserBySchoolName(@Param("emailAddress")String email,int age ,String schoolName);
}
