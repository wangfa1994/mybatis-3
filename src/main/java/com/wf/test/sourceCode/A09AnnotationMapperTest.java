package com.wf.test.sourceCode;

import com.wf.mapper.IUserAnnMapper;
import com.wf.mapper.IUserMapper;
import com.wf.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/* 注解映射 */
public class A09AnnotationMapperTest {

  public static void main(String[] args) throws Exception {
    findByIdMapper();

  }

  public static void findByIdMapper() throws IOException {
    // 加载配置文件的时候，会根据类路径的全名称进行加载mapper资源,
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a09/sqlMapConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    // 在最初的ibatis,我们的查询是通过nameSpace+id进行查询处理的,后来进行迭代升级面向mapper编程之后，将我们的mapper类与namespace相同，进行mapper开发
    IUserAnnMapper mapper = sqlSession.getMapper(IUserAnnMapper.class);
    User user = mapper.findById(1);
    System.out.println(user);


    User user1 = mapper.findByIdProvider(1);
    System.out.println(user1);

  }
}
