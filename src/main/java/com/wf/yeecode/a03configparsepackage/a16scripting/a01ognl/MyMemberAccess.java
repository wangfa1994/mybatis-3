package com.wf.yeecode.a03configparsepackage.a16scripting.a01ognl;

import ognl.MemberAccess;
import ognl.OgnlContext;

import java.lang.reflect.Member;
import java.util.Map;

public class MyMemberAccess implements MemberAccess {


  @Override
  public Object setup(OgnlContext context, Object target, Member member, String propertyName) {
    System.out.println("into ...");
    // 设置成员的可访问性
    return null;
  }

  @Override
  public void restore(OgnlContext context, Object target, Member member, String propertyName, Object state) {

  }

  @Override
  public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
    // 通过这个进行得到对应的是否可以进行反射的
    System.out.println("propertyName:"+propertyName+"   member:"+member);
    return true;
  }
}
