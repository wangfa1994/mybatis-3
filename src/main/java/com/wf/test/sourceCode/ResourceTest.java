package com.wf.test.sourceCode;

import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceTest {

  public static void main(String[] args) throws IOException {

    // class 加载资源 和 classLoad 加载资源的方式是不同的

    // classLoad加载资源   resource 的加载会去classpath下面去找对应的文件资源，不需要使用/进行引导到资源文件夹中直接使用
    // 使用classLoad 进行加载的时候，默认是不要使用 / 引导的
    // org.apache.ibatis.io.ClassLoaderWrapper.getResourceAsStream(java.lang.String, java.lang.ClassLoader[]) 这个方法中有注释说明
    ClassLoader classLoader = ResourceTest.class.getClassLoader(); //URLClassLoader

    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader(); // URLClassLoader

    InputStream resourceAsStream = classLoader.getResourceAsStream("jdbc.properties");
    Properties properties = new Properties();
    properties.load(resourceAsStream);
    System.out.println(properties);

    resourceAsStream = classLoader.getResourceAsStream("resourcesdemo.properties");
    properties.load(resourceAsStream);
    System.out.println(properties);


    Properties resourceAsProperties = Resources.getResourceAsProperties("jdbc.properties");
    System.out.println("使用mybatis进行加载"+resourceAsProperties);

     resourceAsProperties = Resources.getResourceAsProperties("resourcesdemo.properties");
    System.out.println("使用mybatis进行加载"+resourceAsProperties);


    // Class 进行资源加载 这个会先进行路径的处理，然后得到处理的路径之后，在进行classLoad进行加载
    // 在处理路径的时候，会根据是否存在/开头进行解析最终的路径，如果不带/，则会解析当前class的全路径，然后进行资源的路径转换,
    Class<ResourceTest> resourceTestClass = ResourceTest.class;

    InputStream resourceAsStream2 = resourceTestClass.getResourceAsStream("/resourcesdemo.properties");
    properties.load(resourceAsStream2);
    System.out.println(properties);


    InputStream resourceAsStream1 = resourceTestClass.getResourceAsStream("resourcesdemo.properties");
    properties.load(resourceAsStream1);
    System.out.println(properties);




  }
}
