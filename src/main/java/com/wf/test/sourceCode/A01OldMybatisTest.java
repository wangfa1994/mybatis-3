package com.wf.test.sourceCode;

import com.wf.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// mybatis的开发模式有两种一种是原始开发，一种是mapper开发
public class A01OldMybatisTest {
  public static void main(String[] args) throws Exception {
    // 原始的增删改查使用
    //select();
    //saveUser();
    //select();
    //updateUser();

    // deleteUser();
    findById();
  }

  public static void findById() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a01/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession(); //DefaultSqlSession 主要类，sql
    // 通过sql
    User user = (User)sqlSession.selectOne("user.findById", 1);
    System.out.println(user);
    sqlSession.close();
  }

  public static void select() throws IOException {
    //1.Resources工具类，配置文件的加载，把配置文件加载成字节输入流
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a01/sqlMapConfig.xml");
    //2.解析了配置文件，并创建了sqlSessionFactory工厂 // 返回的是 DefaultSqlSessionFactory 主要类
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    //3.生产sqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();// 默认开启一个事务，但是该事务不会自动提交
    //在进行增删改操作时，要手动提交事务
    //4.sqlSession调用方法：查询所有selectList  查询单个：selectOne 添加：insert  修改：update 删除：delete
    // 在最初的ibatis,我们的查询是通过nameSpace+id进行查询处理的,后来进行迭代升级面向mapper编程之后，将我们的mapper类与namespace相同，进行mapper开发
    List<User> users = sqlSession.selectList("user.findAll");
    for (User user : users) {
      System.out.println(user);
    }
    sqlSession.close();

  }



  public static void saveUser() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a01/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession(true);//事务自动提交

    User user = new User();
    user.setId(6);
    user.setUsername("tom");
    user.setPassword("123456");
    user.setBirthday("2024-12-18");
    sqlSession.insert("user.saveUser", user);
    sqlSession.close();
  }

  public static void updateUser() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a01/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    User user = new User();
    user.setId(6);
    user.setUsername("lucy");
    sqlSession.update("user.updateUser", user);
    sqlSession.commit();

    sqlSession.close();
  }


  public static void deleteUser() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a01/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();


    sqlSession.delete("user.deleteUser", 6);
    sqlSession.commit();

    sqlSession.close();
  }

}
