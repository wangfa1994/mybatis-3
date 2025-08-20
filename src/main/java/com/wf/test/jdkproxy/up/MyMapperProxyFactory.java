package com.wf.test.jdkproxy.up;

import com.wf.test.jdkproxy.MyMapperProxy;

import java.lang.reflect.Proxy;

public class MyMapperProxyFactory<T> {

  private final Class<T> mapperInterface;

  public MyMapperProxyFactory(Class<T> t) {
    this.mapperInterface = t;
  }

  @SuppressWarnings("unchecked")
  T createMapperProxy(){
    T o = (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, new MyMapperProxy());
    return o;
  }
}
