package com.wf.yeecode.a02basepackage.a02reflection;

import com.wf.yeecode.a01.User;
import com.wf.yeecode.a02basepackage.a02reflection.a02Reflection.Dog;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;

/**
通过 Java反射，能够在类的运行过程中知道这个类有哪些属性和方法，还可以修改属性、调用方法、建立类的实例
四个功能：在运行时判断任意一个对象所属的类，在运行时构造任意一个类的对象，在运行时修改任意一个对象的成员变量，在运行时调用任意一个对象的方法。

 通过反射可以很方便地调用对象的方法和读写方法的属性

 {@link java.lang.reflect.Type}  Type接口及其子类 ：Type是Java编程语言中所有类型的公共超接口。这些类型包括原始类型、参数化类型、数组类型、类型变量和基本类型。存在较多的子类，其中Class类就是其中的一个
 {@link Class}Class类 代表运行的java程序中的类和接口，枚举类型(属于类)，注解(属于接口)也都是Class类的子类


* */
public class A02ReflectionDemo {

  /* mybatis 反射包reflect包
    1. reflection包下的factory子包：对象工厂子包，用来基于反射创建出各种对象(一个接口ObjectFactory用来定义规范，一个实现DefaultObjectFactory用于实现)
    mybatis所需要的对象就是通过ObjectFactory进行创建出来的

    2. reflection 包下的 invoker 子包是执行器子包，该子包中的类能够基于反射实现对象方法的调用和对象属性的读写。进一步封装和简化反射调用对象的方法和读写方法的属性的操作
    Invoker接口：定义规则 只有两个方法
    GetFieldInvoker实现： 负责对象属性的读操作
    SetFieldInvoker实现：负责对象属性的写操作
    MethodInvoker实现：负责对象其他方法的操作
    AmbiguousMethodInvoker实现：

    3.reflection包下的 property子包是属性子包，该子包中的类用来完成与对象属性相关的操作。存在两个对象user1 和user2,我们想要user2中的属性值和user1一致，使用setter方法进行一个一个得到获取，属性值copy
    借助PropertyCopier类可以轻松完成赋值。使用spring的属性值copy也是可以的，只不过是mybatis重新封装了自己的。
    借助PropertyNamer可以轻松的从符合Java bean规范的方法名中查出来对应的属性值
    借助PropertyTokenizer 是一个属性标记器，进行符合标准的属性的拆分

    4.reflection包下的子包wrapper对象包装器子包,该子包中的类使用装饰器模式对各种类型的对象(基本Bean对象，集合对象，map对象)进行进一步封装，为其增加功能，可以方便的进行解析操作方法属性 【包中uml类图关系】

    5.反射核心类：Reflector类， Reflector类负责对一个类进行反射解析，并将解析后的结果在属性中存储起来，【ReflectFactory接口 DefaultReflectorFactory实现类 Reflector类是核心输出】

    6.反射包装类 ，将一些基本操作进行包装成对应的操作类，更简单易用，wrapper中的保证类，MetaClass 和 MetaObject 类 包装了class类和业务对象类

    7. 异常拆包工具  ExceptionUtil 是一个异常工具类，它提供一个拆包异常的工具方法 unwrapThrowable。该方法将 InvocationTargetException 和 UndeclaredThrowableException 这两类异常进行拆包，得到其中包含的真正的异常。

    8. 参数名解析器 ParamNameResolver 用来按照顺序列出方法中的虚参，并对实参进行名称标注？

    9. 泛型解析器  TypeParameterResolver  ？




  * */


  public static void main(String[] args) throws Exception {
    Field[] declaredFields = User.class.getDeclaredFields();
   // Field[] declaredFields1 = User.class.getSuperclass().getDeclaredFields();


    propertyTokenizer();
//    copyProperties();



  }

  private static void copyProperties() throws NoSuchMethodException {
    User user = new User();
    user.setId(1);
    user.setName("zhangsan");
    user.setEmail("zhangsan@qq.com");
    user.setAge(12);
    user.setSex(1);
    user.setSchoolName("学校名称");
    User user2 = new User();
    PropertyCopier.copyBeanProperties(user.getClass(),user,user2);
    System.out.println(user2.getName());

    /*引用对象拷贝是浅拷贝*/
    Children children = new Children();children.setAge("12");children.setName("children");
    Parent parent  = new Parent();parent.setAge("32");parent.setName("parent");parent.setChildren(children);
    Parent parentDestination = new Parent();
    PropertyCopier.copyBeanProperties(parent.getClass(),parent,parentDestination);
    System.out.println(parentDestination);

    /*继承的属性进行copy*/
    Dog dog = new Dog(); dog.setName("dog"); dog.setAge("12"); dog.setColor("black");
    Dog dogDestination = new Dog();
    PropertyCopier.copyBeanProperties(dog.getClass(),dog,dogDestination);
    System.out.println(dogDestination.getName());

    //



  }

  public  static void propertyNamer() throws NoSuchMethodException {
    String s = PropertyNamer.methodToProperty(Dog.class.getMethod("getColor").getName());
    System.out.println("获得属性"+s);
  }
  public static void propertyTokenizer(){
    PropertyTokenizer tokenizer = new PropertyTokenizer("Parent[sid].name");
    System.out.println(tokenizer.getName()+"::"+tokenizer.getIndexedName()+"::"+tokenizer.getIndex()
    +"::"+tokenizer.getChildren()); /* name = Parent; indexedName = Parent[sid]; index=sid;children=name;*/

  }



  static class Parent{
    private String name;
    private String age;
    private Children children;

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

    public Children getChildren() {
      return children;
    }

    public void setChildren(Children children) {
      this.children = children;
    }
  }
  static class Children{
    private String name;
    private String age;

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
}
