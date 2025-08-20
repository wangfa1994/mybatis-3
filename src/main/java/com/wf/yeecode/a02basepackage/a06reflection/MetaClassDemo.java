package com.wf.yeecode.a02basepackage.a06reflection;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;

import java.util.Arrays;

public class MetaClassDemo {

  private String name;
  private String age;


  public static void main(String[] args) {

    /*
    * MetaClass 是对类元信息的包装。类元信息一般是指Class对象
    * */

    ReflectorFactory factory = new DefaultReflectorFactory();

    MetaClass metaClass = MetaClass.forClass(MetaClassDemo.class,factory);
    String[] setterNames = metaClass.getSetterNames();
    System.out.println(Arrays.toString(setterNames));

    String property = metaClass.findProperty("name");
    System.out.println(property);


  }


  public String sayHello(String param){

    return  "sayHello"+param;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAge() {
    return age;
  }

  public void setAge(String age) {
    this.age = age;
  }
}
