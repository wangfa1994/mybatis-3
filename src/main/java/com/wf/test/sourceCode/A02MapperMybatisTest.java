package com.wf.test.sourceCode;


import com.wf.mapper.IUserMapper;
import com.wf.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class A02MapperMybatisTest {

  public static void main(String[] args) throws Exception {
    findByIdMapper();

  }

  public static void findByIdMapper() throws IOException {
    // 加载配置文件的时候，会根据类路径的全名称进行加载mapper资源,
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a02/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    // 在最初的ibatis,我们的查询是通过nameSpace+id进行查询处理的,后来进行迭代升级面向mapper编程之后，将我们的mapper类与namespace相同，进行mapper开发
    IUserMapper mapper = sqlSession.getMapper(IUserMapper.class);
    User user = mapper.findById(1);
    System.out.println(user);



  }

  public static void findAllMapper() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a02/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    // 在最初的ibatis,我们的查询是通过nameSpace+id进行查询处理的,后来进行迭代升级面向mapper编程之后，将我们的mapper类与namespace相同，进行mapper开发
    IUserMapper mapper = sqlSession.getMapper(IUserMapper.class);
    List<User> all = mapper.findAll();
    for (User user : all) {
      System.out.println(user);
    }


  }

  public static void test3() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a02/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    IUserMapper mapper = sqlSession.getMapper(IUserMapper.class);

    User user1 = new User();
    user1.setId(4);
    user1.setUsername("lucy");

    List<User> all = mapper.findByCondition(user1);
    for (User user : all) {
      System.out.println(user);
    }


  }

  public static void test4() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    IUserMapper mapper = sqlSession.getMapper(IUserMapper.class);

    int[] arr = {1, 2};

    List<User> all = mapper.findByIds(arr);
    for (User user : all) {
      System.out.println(user);
    }

  }

  public static void test5() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a02/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();


    sqlSession.delete("com.wf.dao.IUserDao.deleteUser", 6);
    sqlSession.commit();

    sqlSession.close();
  }


  /**
   * org.apache.ibatis.session.Configuration
   * org.apache.ibatis.builder.BaseBuilder 存在configuration变量 存在TypeAliasRegistry 变量
   *
   * org.apache.ibatis.mapping.MappedStatement 解析的我们的sql的xml
   *
   * org.apache.ibatis.transaction.TransactionFactory
   *
   * 字节码操作库 cglib , javassist , asm , byteBuddy , objenesis 等这些类库主要是用于操作字节码 。jdk的动态代理也是运行时产生新的字节码
   * 而aspectJ 是属于一个面向切面编程的一个框架，在这个框架中，他也涉及到了针对字节码的操作他没有直接提供操作字节码的方法
   *
   * mybatis 推荐使用javassist库而不再是cglib了
   *
   * Executor体系 是属于装饰者模式 代理模式？
   *
   *
   *
   * SqlSessionFactory接口 主要用于创建我们的sqlSession，而且在创建过程中会创建Executor执行器，最后用于得到我们的Transaction,这里得到我们的Connection
   * SqlSession接口  使用MyBatis的主要Java接口。通过这个接口，您可以执行命令、获取映射器和管理事务。
   * TransactionFactory接口 用于创建我们的Transaction，
   * Transaction 接口 封装了 数据库连接。处理连接生命周期，包括：连接的创建、准备、提交回滚和关闭。
   *
   * SqlSessionFactory在获得到SqlSession的时候，会进行Transaction的创建，然后会丢到Executor执行器中，在进行执行的时候，会利用Executor进行执行，
   *
   *
   * StatementHandler 体系  用于获得 Statement
   * ResultHandler 体系 用于处理ResultSet
   *
   *
   *
   * 一级缓存 和二级缓存
   * 一次缓存sqlsession级别的缓存，
   * 二级缓存mapper级别的缓存
   *
   * CacheKey 缓存key，主要由： 命名空间id limit参数offset和limit  sql语句  参数值 环境值 六部分构成，通过hash处理。 updateList属性存放了我们的单个值
   *
   *
   *
   * mapper的代理是基于jdk的动态代理，因为这个是面向接口的，所以可以直接使用jdk的动态代理即可。而不需要使用cglib或者javassist等，但是在其他模块使用了cglib代理
   * 在我们查询到结果之后，需要将结果转成对象的时候，使用了代理，配置在Configuration中的 ProxyFactory 属性
   *
   */


}
