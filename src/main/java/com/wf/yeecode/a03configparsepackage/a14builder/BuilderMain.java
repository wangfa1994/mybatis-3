package com.wf.yeecode.a03configparsepackage.a14builder;

public class BuilderMain {

  /* 建造者模式，并且存在两个功能，解析xml 和 解析注解mapper

     BaseBuilder类是所有建造者的基类,虽然被声明成一个抽象类，但是本身不含有任何的抽象方法，更像是一个工具类
        XMLConfigBuilder XMLScriptBuilder XMLMapperBuilder MapperBuilderAssistant XMLStatementBuilder

        SqlSourceBuilder ParameterMappingTokenHandler

    通过一系列的*Builder，*Resolver ,用来解析处理对应的配置标签

    XML子包 可以说是用来解析配置文件和映射文件的入口。不同的结点有不同的解析器进行解析
    配置文件：
    XMLConfigBuilder: 配置文件的解析类，会使用解析的结果建造出一个Configuration对象
    XMLMapperEntityResolver: xml的声明文件头的解析器 用于获取xml头部对应的dtd本地文件流
    映射文件：
    XMLMapperBuilder:
    XMLMapperEntityResolver:
    XMLStatementBuilder

    注解映射解析
    MapperAnnotationBuilder


  *
  * */
}
