package com.wf.yeecode.a04corepackage.a24plugin.custPlugin;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;

public class MainTest {
  public static void main(String[] args) throws Exception {
    String resource = "yeecode/corepackage/a24/mybatis-config.xml";
    InputStream inputStream = null;
    inputStream = Resources.getResourceAsStream(resource);

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession session = sqlSessionFactory.openSession();

    UserMapper userMapper = session.getMapper(UserMapper.class);

    User userParam = new User();
    userParam.setSchoolName("Sunny School");
    List<User> userList = userMapper.queryUserBySchoolName(userParam);
    // 打印查询结果
    for (User user : userList) {
      //System.out.println("name : " + user.getName() + " ;  email : " + user.getEmail());
      System.out.println(user);
    }

    session.close();
  }

  /* 相关拦截点的添加是在创建相关对象的时候进行添加的，如果有存在的拦截器则会创建出一个Plugin的代理类，然后在Plugin的代理类中进行拦截器的处理

  在Configuration类中，newParameterHandler newResultSetHandler  newStatementHandler newExecutor 创建各个对象的时候进行判断是否需要产生对应的代理类，然后进行处理

  *
  * */
}
