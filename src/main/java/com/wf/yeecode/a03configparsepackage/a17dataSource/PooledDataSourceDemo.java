package com.wf.yeecode.a03configparsepackage.a17dataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class PooledDataSourceDemo {

  public static void main(String[] args) throws SQLException {

    /*
    * PooledDataSource 使用了代理进行数据源的池化技术
    * 数据源链接是存放在对应的PoolState状态中,
    * 数据源是通过内置的一个非包装的数据源UnpooledDataSource中得到一个又一个的链接
    * 然后得到的真实链接Connection链接会被封装代理成PooledConnection链接对象，代理逻辑在PooledConnection中。
    * 然后再进行使用的时候，实际上是使用的代理类，所有的逻辑都会进行到逻辑PooledConnection中，这样的话，就可以通过代理进行拦截到Close方法
    * 然后Connection再调用Close方法的时候，使用的是归还到池中，而不是真正的关闭
    *
    * 区别于线程池，线程池是使用创建线程之后，从对应的队列中一直循环取出数据
    *
    * */
    PooledDataSource pooledDataSource = new PooledDataSource();
    pooledDataSource.setDriver("com.mysql.cj.jdbc.Driver");
    pooledDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/yeecode?serverTimezone=UTC");
    pooledDataSource.setUsername("root");
    pooledDataSource.setPassword("root");
    pooledDataSource.setPoolMaximumActiveConnections(2);
    Connection connection = pooledDataSource.getConnection();
    Connection connection1 = pooledDataSource.getConnection();
    System.out.println(connection);
    System.out.println(connection1);
    connection.close();



    //pooledDataSourceFactory();

  }

  private static void pooledDataSourceFactory() throws SQLException {
    PooledDataSourceFactory sourceFactory = new PooledDataSourceFactory();
    Properties properties  = new Properties();
    properties.put("driver","com.mysql.cj.jdbc.Driver");
    properties.put("url","jdbc:mysql://127.0.0.1:3306/yeecode?serverTimezone=UTC");
    properties.put("username","root");
    properties.put("password","root");
    properties.put("poolMaximumActiveConnections","2");
    sourceFactory.setProperties(properties);
    DataSource dataSource = sourceFactory.getDataSource();

    Connection connection = dataSource.getConnection();
    Connection connection1 = dataSource.getConnection();
    Connection connection2 = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement("select * from user");
    ResultSet rs = preparedStatement.executeQuery();
    while (rs.next()){
      System.out.println(rs.getInt(1) + "\t"
        + rs.getString(2) + "\t"
        + rs.getString(3) + "\t"
        + rs.getString(4));
    }
    connection.close();

    Connection connection3 = dataSource.getConnection();
    System.out.println(connection3);
  }
}
