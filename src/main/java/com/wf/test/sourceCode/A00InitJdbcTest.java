package com.wf.test.sourceCode;

import java.sql.*;

public class A00InitJdbcTest {

  public static void main(String[] args) throws Exception {

    // https://blog.csdn.net/sinat_28978689/article/details/56522072


    //add();
    //select();
    selectPrepareStatement();

    /**
     * JDBC 是一个标准的 API，定义了一系列接口和类，使得 Java 程序可以以统一的方式访问不同的数据库,数据库供应商通常会提供 JDBC 驱动程序，这些驱动程序实现了 JDBC 规范，使得 Java 程序可以通过 JDBC API 访问数据库
     * DriverManager: 用于管理和注册 JDBC 驱动程序, 提供方法来建立数据库连接getConnection
     * Connection: 与特定数据库的连接（会话）。在连接上下文中执行 SQL 语句并返回结果，可以利用这个信号的createStatement()方法，创建一个 Statement 或者 PreparedStatement 对象来将 SQL 语句发送到数据库
     * Statement: 用于执行静态 SQL 语句并返回它所生成结果的对象。利用executeQuery方法，我们可以获得结果的对象ResultSet
     * PreparedStatement：用于执行预编译SQL，可以提高性能并防止 SQL 注入攻击。
     * ResultSet: 结构接收器，可以从对象中获得我们对应的数据
     */

  }


  private static void add() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/lagou?useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true",
      "root", "root");

    // 使用prepareStatement进行处理 , preparedStatement 直接进行预编译，利用占位符可以有效防治sql注入
    PreparedStatement preparedStatement = connection.prepareStatement("insert into  user values (?,?,?,?)");
    preparedStatement.setInt(1, 6);
    preparedStatement.setString(2, "username");
    preparedStatement.setString(3, "password");
    preparedStatement.setString(4, "2022-12-16");
    int i = preparedStatement.executeUpdate();
    System.out.println("新增的记录数:" + i);

  }


  private static void select() throws SQLException {
    // 1. 加载驱动 Driver
    //Class.forName("com.mysql.cj.jdbc.Driver");
    // 2. 获取链接 Connection
    Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/lagou?useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true",
      "root", "root");

    // 3.  创建语句 Statement
    Statement statement = connection.createStatement();

    // 4.  执行语句
    ResultSet rs = statement.executeQuery("select * from user");
    // 5. 结果输出
    while (rs.next()) {
      System.out.println(rs.getInt(1) + "\t"
        + rs.getString(2) + "\t"
        + rs.getString(3) + "\t"
        + rs.getString(4));
    }
    // 6. 关闭链接
    connection.close();
  }

  private static void selectPrepareStatement() throws SQLException {
    // 1. 加载驱动 Driver
    //Class.forName("com.mysql.cj.jdbc.Driver");
    // 2. 获取链接 Connection
    Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/lagou?useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true",
      "root", "root");

    // 3.  创建语句 Statement
    PreparedStatement preparedStatement = connection.prepareStatement("select * from user");

    // 4.  执行语句
    ResultSet rs = preparedStatement.executeQuery();

    // 5. 结果输出
    while (rs.next()) { // 根据字段名称进行输出，这种格式可以不按照字段定义
      System.out.println(rs.getInt("id") + "\t"
        + rs.getString("password") + "\t"
        + rs.getString("username") + "\t"
        + rs.getString("birthday"));
    }

    // 6. 关闭链接
    connection.close();
  }


}
