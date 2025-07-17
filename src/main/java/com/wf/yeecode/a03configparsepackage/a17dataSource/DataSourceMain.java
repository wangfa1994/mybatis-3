package com.wf.yeecode.a03configparsepackage.a17dataSource;

public class DataSourceMain {

  /**
   *
   *
   * DataSourceFactory包： 作为所有工厂的接口，javax.sql包中的 DataSource作为所有工厂产品的接口 【工厂方法模式】
   *
   *
   * JNDI数据源工厂:
   * jdni:(java Naming and Directory Interface) 是Java命名和目录接口，它能够为Java应用程序提供命名和目录访问的接口，我们可以将其理解为一个命名规范。
   * 在使用该规范为资源命名并将资源放入环境(Context)中后，可以通过名称从环境中查找(lookup)对应的资源。
   * 数据源作为一个资源，就可以使用JNDI命名后放入环境中，这就是JNDI数据源，之后就可以通过名称信息，将该数据源查找出来
   * JndiDataSourceFactory的作用就是从环境中找出指定的 JNDI数据源
   *
   *
   *
   *
   *
   *
   *
   *  java.sql包
   *  java.sql包是JDBC的核心API包，为Java提供了访问数据源中数据的基础功能，基于此包将实现SQL语句传递给数据库，从数据库中以表格的形式读写数据的功能
   *
   *  Driver接口: 数据库驱动的接口
   *  不同种类的数据库厂商只需根据自身数据库特点开发相应的 Driver实现类，并通过 DriverManager进行注册, JDBC便可以连接不同公司不同种类的数据库
   *
   * DriverManager类: jdbc驱动程序管理器，管理一组JDBC驱动程序，主要功能就是得到一个面向数据库的链接对象Connection
   * Connection接口:  数据库连接的类 ，基于这个链接类，可以完成 SQL语句的执行和结果的获取等工作
   * Statement接口: 数据库操作语句  用来执行静态 SQL语句并返回结果。通常 Statement对象会返回一个结果集对象 ResultSet
   * ResultSet类: 数据库操作结果
   *
   *
   * javax.sql包
   * JDBC扩展 API包 ， 扩展了 JDBC核心 API包的功能，提供了对服务器端的支持，是 Java企业版的重要部分
   *
   * DataSource接口： 获取面向数据源的 Connection对象，与 java.sql 中直接使用 DriverManager建立连接的方式相比更为灵活 [DataSource接口的实现中也是通过 DriverManager对象获取 Connection对象的]
   * dataSource仅仅是一个接口，不同的数据库可以为其提供多种实现.
   * 基本实现：生成基本的到数据库的链接对象Connection
   * 连接池实现：生成的Connection对象能够自动加到连接池中
   * 分布式事务实现：生成的Connection对象能够参与分布式事务
   *
   * javax.sql还提供了连接池、语句池、分布式事务等方面的诸多特性
   *
   *
   *
   * */

}
