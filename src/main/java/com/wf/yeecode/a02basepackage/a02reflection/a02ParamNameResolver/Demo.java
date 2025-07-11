package com.wf.yeecode.a02basepackage.a02reflection.a02ParamNameResolver;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Demo {

  public static void main(String[] args) throws Exception {

    resolverDemoMethodForTwo();
   // resolverDemoMethod();
//      resolver();
//    mybatisResolver();


  }

  private static void mybatisResolver() {
    String resource = "yeecode/basepackage/a02ParamNameResolver/mybatis-config.xml";
    InputStream inputStream = null;
    try {
      inputStream = Resources.getResourceAsStream(resource);
    } catch (IOException e) {
      e.printStackTrace();
    }

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

    try (SqlSession session = sqlSessionFactory.openSession()) {
      UserMapper userMapper = session.getMapper(UserMapper.class); // 这里得到是我们的代理对象
      // 调用接口展开数据库操作
      List<User> userList =  userMapper.queryUserBySchoolName("jack@sample.com",25,"Sunny School");
      // 打印查询结果
      for (User user : userList) {
        System.out.println("name : " + user.getName() + " ;  email : " + user.getEmail());
      }
    }
  }

  private static void resolver() throws NoSuchMethodException {
    Configuration configuration = new Configuration();
    configuration.setUseActualParamName(true);
    Method method = UserMapper.class.getMethod("queryUserBySchoolName", String.class, int.class, String.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration,method); // 初始化的时候会进行解析
    // ParamNameResolver 主要用来解析mapper中的方法和映射关系，以及调用时的实参对应关系
    String[] names = resolver.getNames();
    System.out.println(Arrays.toString(names));
    Object[] args = {"jack@sample.com",25,"Sunny School"};
    Object namedParams = resolver.getNamedParams(args); //  将我们的实参参数列表进行传递，可以进行匹配到对应的形参上面。
    System.out.println(namedParams);

  }

  private static void resolverDemoMethod() throws Exception{
    Configuration configuration = new Configuration();
    configuration.setUseActualParamName(true);
    Demo demo = new Demo();
    demo.sayHello("原始调用");

    Method method = Demo.class.getMethod("sayHello", String.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration,method); // 将我们的方法进行封装
    // 设置方法的参数
    Object[] args = {"ParamNameResolver调用"};
    Object namedParams = resolver.getNamedParams(args);
    method.invoke(demo,namedParams);


  }

  public  void sayHello(String hello){
    System.out.println("hello:"+hello);
  }


  private static void resolverDemoMethodForTwo() throws Exception{
    Configuration configuration = new Configuration();
    configuration.setUseActualParamName(true);
    Demo demo = new Demo();
    demo.sayHello("张三","原始调用");

    Method method = Demo.class.getMethod("sayHello", String.class, String.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration,method); // 将我们的方法进行封装
    // 设置方法的参数
    Object[] args = {"王五","ParamNameResolver调用"};
    Object namedParams = resolver.getNamedParams(args); //多个参数，返回的是一个map，需要进行拆出来参数值
    //
    MapperMethod.ParamMap<String> params  = (MapperMethod.ParamMap)namedParams;
    method.invoke(demo,params.get("param1"),params.get("param2")); // 并且返回的参数是原有方法的双倍
    method.invoke(demo,params.get("name"),params.get("content")); // 并且返回的参数是原有方法的双倍


  }

  public  void sayHello(String name,String content){
    System.out.println(name+"say:"+content);
  }

  public  void paramAnnotation(@Param("name")@Mapper String name){

  }
}
