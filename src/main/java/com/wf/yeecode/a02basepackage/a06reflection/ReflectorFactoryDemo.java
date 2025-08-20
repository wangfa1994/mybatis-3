package com.wf.yeecode.a02basepackage.a06reflection;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;

import java.util.Arrays;

public class ReflectorFactoryDemo {

  private String name;

  private String age;



  /**
   *  ReflectorFactory  创建Reflector对象的工厂， 传递一个Class ,获得此Class对象的Reflector对象，
   *  Reflector 类将一个类反射解析后，会将该类的属性、方法等一一归类放到此类中的各个属性中，类似于一个提前的组合对象，组合了反射的各个方法属性
   *  用这个类可以轻松的得到相关的类的反射属性
   */
  public static void main(String[] args) {
    ReflectorFactory factory = new DefaultReflectorFactory();
    Reflector forClass = factory.findForClass(ReflectorFactoryDemo.class);
    System.out.println(Arrays.toString(forClass.getSetablePropertyNames()));

    Class<?> type = forClass.getType();
    System.out.println(type);

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

  public String getSay(){
    return "i am say";
  }

}
