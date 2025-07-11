package com.wf.yeecode.a02basepackage.a03annotation;

import org.apache.ibatis.annotations.Param;

public interface UserMapper {
  User queryUserById(@Param("id") int age);
}
