package com.wf.yeecode.a02basepackage.a07annotationLang;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class ParamDemo {

  public static void main(String[] args) {
    String resource = "yeecode/basepackage/a03annotations/mybatis-config.xml";
    InputStream inputStream = null;
    try {
      inputStream = Resources.getResourceAsStream(resource);
    } catch (IOException e) {
      e.printStackTrace();
    }

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

    try (SqlSession session = sqlSessionFactory.openSession()) {
      UserMapper userMapper = session.getMapper(UserMapper.class); // 这里得到是我们的代理对象
      // 调用接口展开数据库操作
      User user =  userMapper.queryUserById(1);
      // 打印查询结果
        System.out.println(user);
    }
  }
}
