package com.wf.yeecode.a03configparsepackage.a16scripting.a01ognl;

import ognl.*;
import org.apache.ibatis.scripting.xmltags.OgnlCache;

public class A01OGNLDemo {

  public static void main(String[] args) throws Exception {
    // OgnlMemberAccess   OgnlClassResolver  OgnlCache

    Student student = new Student();student.setAge("12");student.setName("student");
    Teacher teacher = new Teacher(); teacher.setStudent(student);teacher.setName("teacher");

    Object value = OgnlCache.getValue("student.age", teacher);
    System.out.println(value);


    //source(teacher);
  }

  private static void source(Teacher teacher) throws OgnlException {
    // 可以自定义对应的 DefaultClassResolver DefaultTypeConverter
    DefaultClassResolver defaultClassResolver = new DefaultClassResolver();
    DefaultTypeConverter defaultTypeConverter = new DefaultTypeConverter();
    MyMemberAccess myMemberAccess = new MyMemberAccess();
    OgnlContext context = new OgnlContext(defaultClassResolver,defaultTypeConverter,myMemberAccess); // 上下文
    String value = (String) Ognl.getValue("name",context, teacher,String.class);
    System.out.println(value);
    String studentAge = (String)Ognl.getValue("student.age",context, teacher,String.class); // 表达式，上下文，根对象
    System.out.println(studentAge);
    Ognl.setValue("setName",context, teacher,"setName");
    System.out.println(teacher.getName());

    String student = (String)Ognl.getValue("student.age",context, teacher,String.class);
  }
}
