package com.wf.mapper;

import com.wf.pojo.User;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

public interface IUserAnnMapper {


  @Select(value = "select * from user where id = #{id}")
  User findById(int id);

  /*
  @select @Insert @Update @Delete
  @SelectProvider @InsertProvider @UpdateProvider @DeleteProvider 简介映射
  * */

  @SelectProvider(type = com.wf.mapper.SelectProvider.class,method = "findByIdProvider")
  User findByIdProvider(int id);


}
