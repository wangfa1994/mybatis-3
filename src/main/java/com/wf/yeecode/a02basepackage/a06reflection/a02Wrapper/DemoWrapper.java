package com.wf.yeecode.a02basepackage.a06reflection.a02Wrapper;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoWrapper {


  public static void main(String[] args) {

    Book book = new Book();book.setName("Meth");book.setPrice(new BigDecimal(12.5));
    List<Book> books = new ArrayList<>();books.add(book);
    Student student = new Student();student.setName("wf");student.setAge(18);student.setBooks(books);


    /*使用的全部是默认的工厂 */
    MetaObject metaObject = MetaObject.forObject(student,new DefaultObjectFactory(),new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());

    ObjectWrapper studentWrapper = metaObject.getObjectWrapper();
    String[] getterNames = studentWrapper.getGetterNames();
    System.out.println(Arrays.toString(getterNames));

    // 使用工具类进行包装 SystemMetaObject
    MetaObject metaObject1= SystemMetaObject.forObject(student);
    ObjectWrapper objectWrapper = metaObject1.getObjectWrapper();
    String[] getterNames1 = objectWrapper.getGetterNames();
    System.out.println(Arrays.toString(getterNames1));

  }
}
