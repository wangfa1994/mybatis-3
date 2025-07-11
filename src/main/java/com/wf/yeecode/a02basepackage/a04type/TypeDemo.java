package com.wf.yeecode.a02basepackage.a04type;

import org.apache.ibatis.type.IntegerTypeHandler;

import java.lang.reflect.Type;

public class TypeDemo {

  public static void main(String[] args) {
    IntegerTypeHandler integerTypeHandler = new IntegerTypeHandler();

    Type rawType = integerTypeHandler.getRawType();
    System.out.println(rawType);
  }
}
