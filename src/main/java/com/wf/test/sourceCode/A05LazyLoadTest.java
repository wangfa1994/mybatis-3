package com.wf.test.sourceCode;

import com.wf.pojo.UserLazy;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class A05LazyLoadTest {

  public static void main(String[] args) throws IOException {

    InputStream inputStream = Resources.getResourceAsStream("sourceCode/a05/sqlMapConfig.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);

    SqlSession sqlSession = factory.openSession();

    UserLazy userLazy = sqlSession.selectOne("user.findById", 1); // 懒加载得到的是代理对象
    String username = userLazy.getUsername();
    System.out.println(username);
    System.out.println(userLazy.getOrderList());

  }
}
