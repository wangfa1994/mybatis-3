package com.wf.yeecode.a03configparsepackage.a17dataSource.custpool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

public class PooledConnection implements InvocationHandler {

  private Connection realConnection;


  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return null;
  }


  public  Connection getConnection(){

    return (Connection)Proxy.newProxyInstance(PooledConnection.class.getClassLoader(),new Class[]{Connection.class},new PooledConnection());
  }

}
