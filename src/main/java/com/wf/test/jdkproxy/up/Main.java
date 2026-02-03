package com.wf.test.jdkproxy.up;

import com.wf.test.jdkproxy.MyProxyObject;

public class Main {

  public static void main(String[] args) {

    // 升级版，添加了工厂 ， 通过工厂，我们就可以为不同的对象，创建代理对象了，但是代理的逻辑都是一样的
    MyMapperProxyFactory<MyProxyObject> myMapperProxyFactory = new MyMapperProxyFactory(MyProxyObject.class);

    MyProxyObject mapperProxy = myMapperProxyFactory.createMapperProxy();
    mapperProxy.sayName();

  }
}
