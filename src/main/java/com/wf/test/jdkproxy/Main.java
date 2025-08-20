package com.wf.test.jdkproxy;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Proxy;

public class Main {

  public static void main(String[] args) {

    MyProxyObject myProxyObject1 = (MyProxyObject)Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[]{MyProxyObject.class}, new MyMapperProxy());
    myProxyObject1.sayName();



  }
}
