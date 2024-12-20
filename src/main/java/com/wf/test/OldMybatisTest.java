package com.wf.test;

import com.wf.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OldMybatisTest {
  public static void main(String[] args) throws Exception {
    //select();
    //saveUser();
    //select();
    //updateUser();

    // deleteUser();
    findById();
  }

  public static void findById() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("demo/oldSqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);  // 返回的是 DefaultSqlSessionFactory 主要类
    SqlSession sqlSession = sqlSessionFactory.openSession(); //DefaultSqlSession 主要类，sql
    // 通过sql
    User user = (User)sqlSession.selectOne("user.findById", 1);
    System.out.println(user);

    User user1 = (User)sqlSession.selectOne("user.findById", 1);

    sqlSession.commit(); // 事务提交的情况下，才会进行二级缓存的存放

    // 二级缓存新开session
    SqlSession sqlSessionNew = sqlSessionFactory.openSession();
    User userNew =  sqlSessionNew.selectOne("user.findById", 1);

    /**
     * 缓存的使用：
     * mysql的一级缓存是session级别的，他不是存储在sqlSession中，而是存放的是在Executor中的localCache中，我们可以通过全局配置文件settings的localCacheScope属性手动修改为Statement级别的，LocalCacheScope类中定义。
     * 修改为Statement级别的情况下，每次查询的结束之后，都会进行清空缓存操作。
     * mybatis的sqlSession定义了一些列的数据库操作，但是真正的操作是委派给了Executor，Executor才是我们真正执行操作的地方。
     * 一级缓存使用的缓存key会被包装成CacheKey对象，是使用六个变量进行当做缓存key的，命名空间，参数，分页数据offset 和 limit ，sql语句，环境进行拼接的。
     * 当 SqlSession 执行修改操作时，一级缓存会被清空，然后再执行更新操作，在Executor进行处理
     *
     *
     * 二级缓存是mapper级别的，默认是关闭的，需要通过全局配置的setting配置和对应的mapper中配置Cache进行指定， 主要是存放在了MappedStatement中的Cache变量中。Cache是一个接口，我们可以使用不同的实现进行处理。
     * 二级缓存的缓存key和一级缓存的key是一样的。
     * 二级缓存是包含一级缓存的，二级缓存没有的情况下，一级缓存也会进行缓存，可以通过配置进行更改
     * 二级缓存是存放在 TransactionalCacheManager中进行管理的。 会被封装成 TransactionalCache对象
     *
     * 二级缓存时跨session级别的，如果在同一个session进行多次获取走的还是一级缓存，而不是二级缓存。
     *
     * 二级缓存用的是装饰者模式，而且里面装饰了日志和定时的实现，缓存过期，是在查询的时候进行过期的，不是用任务去执行的，查询的时候先判断是否过期，如果过期了就直接删除返回null，从新获取
     * 二级缓存查询语句默认为true，走二级缓存的。
     * 二级缓存的存放是在事务提交的时候进行存放到赌赢的缓存存储中的。缓存失效有很大可能是第一个的事务没有提交导致缓存没有进入
     *
     *
     * 整体流程:
     *  在开启二级缓存的情况下，是跨session级别的，一级缓存和二级缓存共用同一个缓存key，在查询的时候会先进行二级缓存的查询，如果不存在的话，则会进行一级缓存的查询，如果不存在的情况下，
     *  则会进行数据库的查询，获取到对应的数据后，会直接存放到一级缓存中去，然后再事务提交的时候进行一级缓存的清空，二级缓存的保存。这样的话，其他sqlSession则会进行二级缓存查询到数据。
     *  一级缓存是sqlSession级别的，但是缓存数据真正存储的是在Executor执行器中，
     *  二级缓存的策略cache是在mapperStatement中存储的，二级缓存的cache也使用了装饰者模式进行处理的，因为有日志，定时清除的相关操作，使用装饰者进行一层层的功能添加。真正的数据是存储在
     *
     *  PerpetualCache 这个才是真正的二级缓存存储对象，里面的map属性，其他的都是针对这个对象进行装饰、
     *
     *
     *
     *
     */

    sqlSession.close();
  }

  public static void select() throws IOException {
    //1.Resources工具类，配置文件的加载，把配置文件加载成字节输入流
    InputStream resourceAsStream = Resources.getResourceAsStream("demo/oldSqlMapConfig.xml");
    //2.解析了配置文件，并创建了sqlSessionFactory工厂
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
    InputStream resourceAsStream = Resources.getResourceAsStream("demo/oldSqlMapConfig.xml");
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
    InputStream resourceAsStream = Resources.getResourceAsStream("demo/oldSqlMapConfig.xml");
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
    InputStream resourceAsStream = Resources.getResourceAsStream("demo/oldSqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();


    sqlSession.delete("user.deleteUser", 6);
    sqlSession.commit();

    sqlSession.close();
  }

}
