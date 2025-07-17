package com.wf.yeecode.a02basepackage.a06reflection.typeParameterResolver;

import org.apache.ibatis.reflection.TypeParameterResolver;

import java.lang.reflect.Type;

public class Demo {

  public static void main(String[] args) throws NoSuchMethodException {
    // 使用 类型解析器进行分析 StudentResponse 中的 getInfo方法的具体返回值，这个方法使用了泛型，通过此方法进行得到具体的
    Type getInfo = TypeParameterResolver.resolveReturnType(StudentResponse.class.getMethod("getInfo", String.class), Response.class);
    System.out.println("=="+getInfo); //Response 不能得到具体的

    Type getInfo1 = TypeParameterResolver.resolveReturnType(StudentResponse.class.getMethod("getInfo", String.class), StudentResponse.class);
    System.out.println("=="+getInfo1); // StudentResponse 可以得到具体的

    TypeParameterResolver.resolveFieldType(null,null);
    TypeParameterResolver.resolveParamTypes(null,null);
    TypeParameterResolver.resolveReturnType(null,null);
  }
}


