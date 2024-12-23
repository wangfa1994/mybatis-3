package com.wf.test.sourceCode;

import com.wf.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

// # 与 $ 占位符的区别
public class A03MappedPlaceHolderTest {


  public static void main(String[] args) throws IOException {
    findById();

    //sqlInjectError();
  }




  public static void findById() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a03/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);  // 返回的是 DefaultSqlSessionFactory 主要类
    SqlSession sqlSession = sqlSessionFactory.openSession(); //DefaultSqlSession 主要类，sql
    // 通过sql
    User user = (User) sqlSession.selectOne("user.findById", 1);
    System.out.println(user);

    User user1 = (User) sqlSession.selectOne("user.findById2", 1);
    System.out.println(user1);
  }

  // sql 注入
  public static void sqlInjectError() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a03/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);  // 返回的是 DefaultSqlSessionFactory 主要类
    SqlSession sqlSession = sqlSessionFactory.openSession(); //DefaultSqlSession 主要类，sql
    // 通过sql
    User user = (User) sqlSession.selectOne("user.findById", 1);
    System.out.println(user);

    User user1 = (User) sqlSession.selectOne("user.findById2", "(select id from user where username = 'tom')"); // 可以直接查询到指定人的信息
    System.out.println(user1);


  }

  /**
   * #和 $ 在mappedStatement中解析的过程中，会产生不一样的sqlSource, $产生的是DynamicSqlSource 而 #产生的是RowSqlSource
   *
   * $在查询的时候，获取BoundSql的时候，会将数据进行拼接上去
   *
   * StatementHandler Statement的包装器
   *
   * SqlBound
   *
   */
}
