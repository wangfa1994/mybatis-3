package com.wf.yeecode.a02basepackage.a06reflection.a02ParamNameResolver;

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

    /* ParamNameResolver 类的作用主要是解析对应方法的参数列表，并且会根据传递的参数值，进行参数名和参数值的一一对应
    * 只有一个参数的时候会原样输出，如果多个的话，则会返回map
    *
    * 底层是通过jdk反射进行处理的，ParamNameUtil工具类 ， 使用了Executable抽象类，下面的 Method 和 Constructor类
    *
    *
    * */

    // paramNameResolver();

     //mybatisResolver();

     // resolverDemoMethod();

    resolverDemoMethodForTwo();


  }

  /*使用ParamNameResolver 进行方法参数的解析，并且可以根据参数值，进行参数名和参数值的一一对应，并且会产生双倍的，会自动添加param相关参数 */
  private static void paramNameResolver() throws NoSuchMethodException {
    Configuration configuration = new Configuration();
    configuration.setUseActualParamName(true); // 处理mapper接口中方法的参数是否使用了真实形参进行处理，默认是使用了，可以进行没有标注@paramc参数的解析
    Method method = UserMapper.class.getMethod("queryUserBySchoolName", String.class, int.class, String.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration, method); // 初始化的时候会进行解析
    // ParamNameResolver 主要用来解析mapper中的方法和映射关系，以及调用时的实参对应关系
    String[] names = resolver.getNames();
    System.out.println(Arrays.toString(names));
    Object[] args = {"jack@sample.com", 25, "Sunny School"};
    Object namedParams = resolver.getNamedParams(args); //  将我们的实参参数列表进行传递，可以进行匹配到对应的形参上面。返回一个参数名：实际参数值的Map
    System.out.println(namedParams); //三个会打印6个，因为会添加对应的param1.param2.param3这样的形式，用于xml使用

  }


  /*mybatis的整个流程 */
  private static void mybatisResolver() throws Exception {
    String resource = "yeecode/basepackage/a02ParamNameResolver/mybatis-config.xml";
    InputStream inputStream = null;

    inputStream = Resources.getResourceAsStream(resource);


    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);


    SqlSession session = sqlSessionFactory.openSession();
    UserMapper userMapper = session.getMapper(UserMapper.class); // 这里得到是我们的代理对象
    List<User> userList = userMapper.queryUserBySchoolName("jack@sample.com", 25, "Sunny School");
    for (User user : userList) {
      System.out.println("name : " + user.getName() + " ;  email : " + user.getEmail());
    }

    List<User> userListWithParam = userMapper.queryUserBySchoolNameWithParam("jack@sample.com", 25, "Sunny School");
    for (User user : userListWithParam) {
      System.out.println("name : " + user.getName() + " ;  email : " + user.getEmail());
    }

  }

  /*使用ParamNameResolver进行方法的解析，然后传递实际参数，并通过方法进行调用， 单个参数的调用*/
  private static void resolverDemoMethod() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setUseActualParamName(true);
    Demo demo = new Demo();
    demo.sayHello("原始调用");

    Method method = Demo.class.getMethod("sayHello", String.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration, method); // 将我们的方法进行封装
    // 设置方法的参数
    Object[] args = {"ParamNameResolver调用"};
    method.invoke(demo, args);

    Object namedParams = resolver.getNamedParams(args);
    method.invoke(demo, namedParams);


  }

  public void sayHello(String hello) {
    System.out.println("hello:" + hello);
  }


  /*使用ParamNameResolver进行方法的解析，然后传递实际参数，并通过方法进行调用， 多个参数的调用*/
  private static void resolverDemoMethodForTwo() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setUseActualParamName(true);
    Demo demo = new Demo();
    demo.sayHello("张三", "原始调用");

    Method method = Demo.class.getMethod("sayHello", String.class, String.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration, method); // 将我们的方法进行封装
    // 设置方法的参数
    Object[] args = {"王五", "ParamNameResolver调用"};
    method.invoke(demo,args);// 可以调用

    Object namedParams = resolver.getNamedParams(args); //多个参数，返回的是一个map，需要进行拆出来参数值
    // method.invoke(demo,namedParams); // 调用异常

    MapperMethod.ParamMap<String> params = (MapperMethod.ParamMap) namedParams;
    method.invoke(demo, params.get("param1"), params.get("param2")); // 并且返回的参数是原有方法的双倍
    method.invoke(demo, params.get("name"), params.get("content")); // 并且返回的参数是原有方法的双倍


  }

  public void sayHello(String name, String content) {
    System.out.println(name + "say:" + content);
  }

}
