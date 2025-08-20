package com.wf.yeecode.a02basepackage.a06reflection.objectFactoryDemo;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class ObjectFactoryDemo {

  public static void main(String[] args) {
    /* 针对ObjectFactory的使用 */
    ObjectFactory objectFactory = new DefaultObjectFactory();
    Person person = objectFactory.create(Person.class);
    System.out.println(person);


    List<Class<?>> constructorArgTypes = new ArrayList<>();
    constructorArgTypes.add(String.class);constructorArgTypes.add(List.class);

    List<Object> constructorArgs = new ArrayList<>(); List<String> bookList = new ArrayList<>();bookList.add("html");bookList.add("java");
    constructorArgs.add("name"); constructorArgs.add(bookList);
    Person person1 = objectFactory.create(Person.class, constructorArgTypes, constructorArgs);
    System.out.println(person1.getBookList());


    // 创建List 不能常见对应的泛型，因为泛型在编译的时候会进行泛型擦拭
    List personList = objectFactory.create(List.class);
    personList.add(new Person());
    personList.add("String");
    System.out.println(personList);

    /*java 的泛型是在 编译期 提供类型检查和类型安全的机制，但在 运行时 会擦除所有泛型类型信息，这就是所谓的 泛型擦除机制
    所有泛型类型参数在编译后都会被替换成其上限（默认是 Object）
    为什么设计成泛型擦除 ，为了保持 向后兼容性：Java 5 引入泛型时，JVM 不需要做大的改动，所有泛型代码在编译后都兼容 Java 1.4 及更早版本的字节码
    如何绕过泛型擦除
    使用 TypeToken（如 Gson 库）或 ParameterizedType 获取泛型信息
    如果你需要在运行时保留泛型信息，建议通过子类化或 Type 接口来保留类型结构
    * */



    /*
    *  我们可以通过new根据不同的业务来创建不同的对象，或者在必须的时候使用反射进行创建就行了，为什么还要使用ObjectFactory来反射进行创建
    *  因为抽象， 抽象出来，我们可以根据一个工厂创建出不同的对象，将创建对象的通用逻辑进行封装产生
    *
    * 比如，我们要得到好多好多的不一样的对象，我们是不是要写很多反射代码，我们直接封装成一个简单工厂，你需要什么类型的，给我传递类型，我就给你返回创建好的对象就可以了。
    * mybatis的返回值就是不同的对象，可以简化操作
    * */


  }
}
