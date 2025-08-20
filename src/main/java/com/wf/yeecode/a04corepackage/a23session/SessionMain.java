package com.wf.yeecode.a04corepackage.a23session;

public class SessionMain {

  /**
   *   session包是整个 MyBatis应用的对外接口包，是离用户最近的包，可以触发mybatis对数据库展开操作
   *
   *
   *  SqlSessionFactoryBuilder(建造者类) SqlSessionFactory  SqlSession
   *
   *  DefaultSqlSessionFactory SqlSessionManager DefaultSession
   *
   *
   *
   * SqlSessionFactoryBuilder生成了 SqlSessionFactory，SqlSessionFactory生成了 SqlSession
   *
   * SqlSessionFactoryBuilder根据配置文件创建出 SqlSessionFactory 对象
   *
   * SqlSessionFactory 通过 openSessionFromDataSource 方法得到SqlSession对象
   *
   *
   * session包是整个 MyBatis应用的对外接口包，而executor包是最为核心的执行器包。
   * DefaultSqlSession 类做的主要工作则非常简单——把接口包的工作交给执行器包处理
   *
   * sqlSessionManager类
   * SqlSessionManager既实现了 SqlSessionFactory接口又实现了 SqlSession接口
   * 既实现了工厂，又实现了产品，
   *
   *
   * Configuration类
   * 配置文件 mybatis-config.xml是 MyBatis配置的主入口，包括映射文件的路径也是通过它指明的
   *  Configuration对象中就包含了 MyBatis运行的所有配置信息
   *  Configuration类还对配置信息进行了进一步的加工，为许多配置项设置了默认值，为许多实体定义了别名等
   *  因而Configuration类是MyBatis中极为重要的一个类
   *
   *  BaseBuilder、BaseExecutor、Configuration、ResultMap等近 20个类都在属性中引用了 Configuration对象，
   *  这使得 Configuration对象成了 MyBatis全局共享的配置信息中心，能为其他对象提供配置信息的查询和更新服务
   *
   *  内部类 StrictMap是 HashMap 的子类
   *  1.不允许覆盖其中的键值。即如果要存入的键已经在 StrictMap中存在了，则会直接抛出异常。这一点杜绝了配置信息因为覆盖发生的混乱
   *  2.自动尝试使用短名称再次存入给定数据 (查看put方法)
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   */
  public static void main(String[] args) {

  }
}
