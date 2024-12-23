package com.wf.test.sourceCode;

import com.wf.pojo.User;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

// 单个mapper的处理
public class SingletonMapperMethodTest {


  public static void main(String[] args) throws IOException {

    Enhancer enhancer = new Enhancer();
    enhancer.setCallbackFilter(new CallbackFilter() {
      @Override
      public int accept(Method method) {
        return 0;
      }
    });

    select();
  }


  public static void select() throws IOException {
    //1.Resources工具类，配置文件的加载，把配置文件加载成字节输入流
    InputStream resourceAsStream = Resources.getResourceAsStream("singleton/singletonSqlMapConfig.xml");
    //2.解析了配置文件，并创建了sqlSessionFactory工厂
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    //3.生产sqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();// 默认开启一个事务，但是该事务不会自动提交
    //在进行增删改操作时，要手动提交事务
    //4.sqlSession调用方法：查询所有selectList  查询单个：selectOne 添加：insert  修改：update 删除：delete
    // 在最初的ibatis,我们的查询是通过nameSpace+id进行查询处理的,后来进行迭代升级面向mapper编程之后，将我们的mapper类与namespace相同，进行mapper开发
    User user = sqlSession.selectOne("user.findById",1);

    System.out.println(user);

    sqlSession.close();
    /**
     *
     * 和源码架构相关
     *
     * 第一步：解析我们的配置文件
     *
     * BaseBuilder抽象类，解析配置文件生成Configuration对象的基础类，解析的都进行了继承，
     *
     * XMLConfigBuilder 用于解析配置文件
     * XMLMapperBuilder 用于解析mapper文件
     * MapperBuilderAssistant 解析mapper中的助手 也继承了BaseBuilder
     * XMLStatementBuilder 解析增删改查sql
     * XMLIncludeTransformer 用来解析sql中的包含字段
     * SqlSource接口  解析我们的sql片段 存在DynamicSqlSource 和 RawSqlSource 实现
     *
     * SqlNode接口
     * SqlSourceBuilder实现类 继承了BaseBuilder
     * XMLScriptBuilder
     * TokenHandler
     *
     *
     * 程序员开发相关
     * SqlSessionFactoryBuilder通过建造者得到我们的SqlSessionFactory对象。在建造的过程会进行我们的配置文件的解析，然后将配置文件信息进行保存到DefaultSqlSessionFactory中  默认的为 DefaultSqlSessionFactory
     * DefaultSqlSessionFactory中有一个变量Configuration，可以通过数据源(DataSource)或者连接(Connect)中得到我们的SqlSession
     *
     *
     * SqlSessionFactory主要是得到我们的SqlSession ，SqlSession 是mybatis封装的与sql服务器进行交互的主要接口,通过SqlSession可以进行增删改查，事务提交等。
     * DefaultSqlSession 中也放置了整体的configuration对象，并且还存在一个执行器，defaultSqlSession会把对应的执行操作委派给这个执行器Executor。
     *
     * Executor执行器在执行的时候需要获得到数据库链接，然后才能进行操作，针对于数据的链接，又被封装到Transaction中
     * JDBCTransaction 这个封装了数据库连接和事务条件的相关，数据库的链接需要我们的数据源，所以Transaction中需要存在Connection和DataSource.
     * Executor执行器，先进行参数的处理，然后得到链接执行语句，然后开始处理结果。
     *
     *
     *
     *
     */

  }
}
