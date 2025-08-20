package com.wf.yeecode.a02basepackage.a09io;

import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.util.Properties;

public class ResourcesDemo {

  public static void main(String[] args) throws IOException {

    // 解析我们的配置文件变成Properties，存储
    Properties resourceAsProperties = Resources.getResourceAsProperties("jdbc.properties");
    System.out.println(resourceAsProperties);

  }
}
