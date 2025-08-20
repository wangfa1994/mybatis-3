package com.wf.yeecode.a02basepackage.a06reflection.invokerDemo;

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InvokerDemo {


  /* Invoker 针对运行的封装，通过反射进行相关实现的调用 ，有 MethodInvoker ， GetFieldInvoker ， SetFieldInvoker 实现 使用了策略模式
  *  针对与MethodInvoker，为什么我们需要反射调用啊，我们再调用的时候也要传递相关的实例对象和参数，才能进行方法的调用，为什么还要单独封装成对应的一个反射调用呢
  *
  *  提供给Reflector进行使用，方便调用
  *
  * */
  public static void main(String[] args) throws Exception {

    InvokerDemo invokerDemo = new InvokerDemo();

    Class<InvokerDemo> invokerDemoClass = InvokerDemo.class;

    Method method =  invokerDemoClass.getMethod("sayName", String.class, String.class);

    Invoker methodInvoker = new MethodInvoker(method); // 通过反射进行方法的调用
    System.out.println(methodInvoker.getType());
    Object invoke = methodInvoker.invoke(invokerDemo, new Object[]{"name", "age"});
    System.out.println(invoke);


    Field invokerName1 = invokerDemoClass.getDeclaredField("invokerName");

    Invoker getInvoker = new GetFieldInvoker(invokerName1);
    Object invoke1 = getInvoker.invoke(invokerDemo, null);
    System.out.println(invoke1);
  }

  public void sayName(String name,String age){
    System.out.println("args:"+name);
  }

  private String invokerName="invokerName";

  public String getInvokerName() {
    return invokerName;
  }

  public void setInvokerName(String invokerName) {
    this.invokerName = invokerName;
  }
}
