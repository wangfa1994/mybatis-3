package com.wf.test.jdkproxy.up;

import com.wf.test.jdkproxy.MyProxyObject;

public class Main {

  public static void main(String[] args) {

    // 升级版，添加了工厂
    MyMapperProxyFactory<MyProxyObject> myMapperProxyFactory = new MyMapperProxyFactory(MyProxyObject.class);

    MyProxyObject mapperProxy = myMapperProxyFactory.createMapperProxy();
    mapperProxy.sayName();

  }
}
