package com.wf.mapper;

import org.apache.ibatis.jdbc.SQL;

public class SelectProvider {

  public String findByIdProvider(){
    String string = new SQL().SELECT("*").FROM("user").WHERE("id = #{id}").toString();
    return string;
  }
}
