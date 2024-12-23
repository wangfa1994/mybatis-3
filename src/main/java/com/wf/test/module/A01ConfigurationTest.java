package com.wf.test.module;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.session.Configuration;

// 解析Configuration
public class A01ConfigurationTest {

  //
  public static void main(String[] args) {
    // properties 标签，用来加载我们的properties属性，我们的jdbc可以进行加载处理
    // settings 标签，用来添加我们的全局配置文件，日志相关的，缓存相关的，懒加载相关的等
    // typeAliases 标签 ，类型别名，用在请求参数和响应参数的一些简单名称的映射
    // plugins 标签
    // objectFactory 标签，
    // objectWrapperFactory 标签


    Configuration configuration  = new Configuration();
    // configuration的解析主要是利用了Build模式进行从不同的builder中解析出来配置文件放到我们的配置对象中
    /**
     * BaseBuilder ：解析过程中的抽象类，这个里面放置了一些公共属性
     *
     * XMLConfigBuilder 用来解析我们的配置文件Configuration标签下的，并且返回我们的Configuration对象
     * XPathParser： 用来解析xml的，生成
     * XMLMapperBuilder 用来解析我们的mapper标签下的,
     * MapperBuilderAssistant mapper解析过程中的助手类，这个也继承了BaseBuilder基本类
     *
     * XMLStatementBuilder 用来解析我们的增删改查语句
     * XMLScriptBuilder 用来解析sql语句并返回SqlSource
     * SqlSource 封装我们的sql语句  DynamicSqlSource $占位符  与   RawSqlSource 预编译的  StaticSqlSource
     * BoundSql 封装了我们的sql ParameterMapping
     *
     * MappedStatement 我们的一条sql最终的表现形式
     *
     * 解析过程中使用了大量的Build模式，分步骤的进行创建我们的Configuration对象
     *
     * openSqlSession()
     * DefaultSqlSession 默认的sqlsession
     * TransactionFactory  被封装在environment中，这个是属于environment标签的配置
     * Executor 执行器   SimpleExecutor  CachingExecutor包装类
     * DefaultSqlSession中包含配置文件 和 执行器Executor， 而在Executor中包含关于事务相关的Transaction，Transaction中包含connection中，
     *
     *查询
     * 参数解析
     * StatementHandler  BaseStatementHandler抽象类  RoutingStatementHandler  PreparedStatementHandler
     * ParameterHandler  DefaultParameterHandler 参数的处理
     * TypeHandler 类型处理，BaseTypeHandler是抽象类，设置参数
     *
     * ResultHandler
     *
     *
     *
     *
     */

  }
}
