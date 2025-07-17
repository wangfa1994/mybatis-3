package com.wf.test.sourceCode;

import com.wf.singleMapper.IUserMapper;
import com.wf.singleMapper.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class A10SingleSelectMapperTest {

  public static void main(String[] args) throws IOException {

      InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a10/sqlMapConfig.xml");
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
      SqlSession sqlSession = sqlSessionFactory.openSession(); //DefaultSqlSession 主要类，sql
      // 通过sql
    IUserMapper mapper = sqlSession.getMapper(IUserMapper.class);
    User user = mapper.findById(1);

    System.out.println(user);
      sqlSession.close();

  }
}
