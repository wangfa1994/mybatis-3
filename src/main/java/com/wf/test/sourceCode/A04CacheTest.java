package com.wf.test.sourceCode;

import com.wf.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class A04CacheTest {


  public static void main(String[] args) throws IOException {
    findByIdForFirst();
   //findByIdForSecond();
  }


  public static void findByIdForSecond() throws IOException {
    InputStream resourceAsStream = null;

    String secondCache = "sourceCode/a04/second/SecondSqlMapConfig.xml"; //二级缓存
    resourceAsStream = Resources.getResourceAsStream(secondCache);
    SqlSessionFactory  sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);  // 返回的是 DefaultSqlSessionFactory 主要类
    // 二级缓存新开session
    SqlSession sqlSessionNew = sqlSessionFactory.openSession();
    User userNew =  sqlSessionNew.selectOne("user.findById", 1);
    System.out.println("newUser:"+userNew);
    sqlSessionNew.commit(); // 事务提交的情况下，才会进行二级缓存的存放 ,close() 也会触发二级缓存的提交

    SqlSession sqlSessionSecond = sqlSessionFactory.openSession();
    User userSecond =  sqlSessionSecond.selectOne("user.findById", 1);
    System.out.println("newUser:"+userSecond);
    System.out.println(userNew ==userSecond);
  }

  public static void findByIdForFirst() throws IOException {

    InputStream resourceAsStream = null;

    String firstCache = "sourceCode/a04/sqlMapConfig.xml"; //一级缓存

    resourceAsStream = Resources.getResourceAsStream(firstCache);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);  // 返回的是 DefaultSqlSessionFactory 主要类
    SqlSession sqlSession = sqlSessionFactory.openSession(); //DefaultSqlSession 主要类，sql
    // 通过sql
    User user = (User)sqlSession.selectOne("user.findById", 1);
    System.out.println(user);

    User user1 = (User)sqlSession.selectOne("user.findById", 1);
    System.out.println(user1);

    System.out.println(user == user1);


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
     * 二级缓存的存放是在事务提交或者关闭的时候进行存放到赌赢的缓存存储中的。缓存失效有很大可能是第一个的事务没有提交导致缓存没有进入，为什么要进行在提交或者关闭的时候才会进行存储呢？
     * 如果直接存储的情况下，这个可能会被其他的事务读取，产生脏读。
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
}
