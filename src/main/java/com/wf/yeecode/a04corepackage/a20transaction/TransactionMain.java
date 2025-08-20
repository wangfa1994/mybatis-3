package com.wf.yeecode.a04corepackage.a20transaction;

public class TransactionMain {

  /**
   *  transaction包是负责进行事务管理的包，该包内包含两个子包：jdbc子包中包含基于JDBC进行事务管理的类，managed子包中包含基于容器进行事务管理的类
   *
   *  事务功能是由数据库提供的,用 Java 进行数据库操作时也可以通过数据库连接 Connection 对事务进行控制。
   *
   *  事务接口和工厂
   *  整个 transaction包采用了工厂方法模式实现，
   *  TransactionFactory 是顶层标准，所有事务工厂的接口，
   *  Transaction是事务的顶层标准，搜友的事务都实现此接口
   *  TransactionFactory接口与 Transaction接口均有两套实现，分别在 jdbc子包和 managed子包中
   *
   * jdbc事务
   *  jdbc子包中存放的是实现 JDBC事务的 JdbcTransaction类及其对应的工厂类 JdbcTransactionFactory
   *
   *  managed容器事务
   *  managed子包中存放的是实现容器事务的 ManagedTransaction类及其对应的工厂类，容器事务是将我们的事务委托给容器进行管理
   *  以 Spring容器为例。当 MyBatis和 Spring集成时，MyBatis中拿到的数据库连接对象是 Spring给出的。
   *  Spring可以通过 XML配置、注解等多种方式来管理事务（即决定事务何时开启、回滚、提交）。
   *  当然，这种情况下，事务的最终实现也是通过 Connection对象的相关方法进行的。整个过程中，MyBatis 不需要处理任何事务操作，全都委托给 Spring即可
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
   *
   *
   *
   */

}
