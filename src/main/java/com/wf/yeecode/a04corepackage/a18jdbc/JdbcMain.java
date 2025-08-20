package com.wf.yeecode.a04corepackage.a18jdbc;

public class JdbcMain {

  /**
   * 1.AbstractSQL类与SQL类
   *
   * AbstractSQL 类是一个抽象类, 包含两个静态内部类：SafeAppendable类和 SQLStatement类 ，一个抽象方法getSelf.
   *
   *
   *
   * jdbc的包存在独立性，和其他包中的数据没有进行交互
   * jdbc包是 MyBatis提供的一个功能独立的工具包，留给用户自行使用而不是由 MyBatis调用
   *
   * SqlRunner类和 ScriptRunner类则为用户提供了执行 SQL语句和脚本的能力。有些情况下，我们要对数据库进行一些设置操作（如运
   * 行一些DDL操作），这时并不需要通过MyBatis提供 ORM功能，那么SqlRunner类和 ScriptRunner类将是非常好的选择
   *
   *
   *
   *
   *
   * */
}
