package com.wf.test.batisProxy;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

public class MapperMethod {


  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {

  }
  // 将方法和sql进行绑定，通过调用方法触发我们的SQL执行 ,sqlSession操作数据库的Java对象，args调用方法时传递的实参
  public Object execute(SqlSession sqlSession, Object[] args) {
    System.out.println("转到对应的执行器中进行执行:参数"+args[0]);
    return "hello simple method";
  }
}
