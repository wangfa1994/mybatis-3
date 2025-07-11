package com.wf.yeecode.a01;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class DemoApplication {
    public static void main(String[] args) {
        // 第一阶段：MyBatis的初始化阶段 该阶段用来完成mybatis运行环境的准备工作，只在mybatis启动时执行一次即可
        String resource = "yeecode/mybatis-config.xml";
        // 得到配置文件的输入流
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource); // 通过resources进行加载配置文件，所需要的类属于io包，负责读取外部文件
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 得到SqlSessionFactory
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream); // 解析配置文件得到Configuration对象，进行创建SqlSessionFactory对象
        /*1.根据配置文件的位置获取对应的InputStream，
          2.从配置文件的根节点开始逐层解析配置文件，也包括相对应的映射文件，解析过程中不断将结果放入到Configuration对象中
          3. 以配置好的Configuration对象为参数，构建一个SqlSessionFactory对象
        *  */



        // 第二阶段：数据读写阶段 改阶段进行数据的读写操作，根据要求完成增删改查数据库操作
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 找到接口对应的实现 我们需要根据映射接口mapper文件找到我们对应的映射配置文件xml
            UserMapper userMapper = session.getMapper(UserMapper.class); // 这里得到是我们的代理对象
            // 组建查询参数
            User userParam = new User();
            userParam.setSchoolName("Sunny School");
            // 调用接口展开数据库操作
            List<User> userList =  userMapper.queryUserBySchoolName(userParam);
            // 打印查询结果
            for (User user : userList) {
                //System.out.println("name : " + user.getName() + " ;  email : " + user.getEmail());
              System.out.println(user);
            }
        }
    }

}
