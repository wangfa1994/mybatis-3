package com.wf.test.batisProxy;

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.wf.test.batisProxy.MapperProxy.MapperMethodInvoker;

public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface; // mapper代理的接口
  private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>(); //缓存这个接口中的方法对应的MapperMethodInvoker,其实现中存在了MapperMethod，methodCache是怎么放进去值的

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public Map<Method, MapperMethodInvoker> getMethodCache() {
    return methodCache;
  }

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy); // MapperProxy 实现了 InvocationHandler接口，jdk动态代理
  }

}
