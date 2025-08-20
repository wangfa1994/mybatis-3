package com.wf.test.batisProxy;

import org.apache.ibatis.session.defaults.DefaultSqlSession;

public class Main {

  public static void main(String[] args) {

    // 拆解mybatis产生代理对象，进行执行逻辑，使用了简单工厂方法得到对象，然后进行方法调用，通过methodHandler转到方法中去

    MapperProxyFactory<ProxyMapper> mapperMapperProxyFactory = new MapperProxyFactory<>(ProxyMapper.class);

    ProxyMapper proxyMapper = mapperMapperProxyFactory.newInstance(new DefaultSqlSession(null, null));

    String s = proxyMapper.sayHello("hello");
    System.out.println(s);

  }
}
