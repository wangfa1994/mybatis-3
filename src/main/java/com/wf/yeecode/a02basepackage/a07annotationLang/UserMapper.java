package com.wf.yeecode.a02basepackage.a07annotationLang;

import org.apache.ibatis.annotations.Param;

public interface UserMapper {
  User queryUserById(@Param("id") int age);
}
