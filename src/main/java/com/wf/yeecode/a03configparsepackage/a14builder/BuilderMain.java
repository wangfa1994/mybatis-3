package com.wf.yeecode.a03configparsepackage.a14builder;

public class BuilderMain {

  /* 建造者模式，并且存在两个功能，解析xml 和 解析注解mapper

     BaseBuilder类是所有建造者的基类,虽然被声明成一个抽象类，但是本身不含有任何的抽象方法，更像是一个工具类
        XMLConfigBuilder : 用来解析配置文件
        XMLScriptBuilder
        XMLMapperBuilder
        MapperBuilderAssistant
        XMLStatementBuilder

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






   抽象类 BaseBuilder
    XMLConfigBuilder类：解析配置文件，配置文件中的相关标签都是此类负责进行解析处理

    映射文件的解析：
    如果是包，需要识别接口，接口上面可能存在注解，需要 MapperAnnotationBuilder类进行处理，没有继承BaseBuilder,但是类的内部装载了一个MapperBuilderAssistant这个实现了BaseBuilder
    MapperAnnotationBuilder则是基于Mapper开发的入口类，不仅会装配MapperBuilderAssistant类,也会使用XMLMapperBuilder进行关联的映射文件资源解析处理。

    XMLMapperBuilder类：解析映射文件，所有的映射文件都会使用此类进行解析处理映射文件中的相关标签
    parameterMap标签会被处理成ParameterMap
    resultMap标签会被处理成ResultMapResolver
    sql标签会被处理成map

    XMLStatementBuilder类：映射文件中的增删改查会委派给此类进行处理
    XMLIncludeTransformer类，用来处理我们增删改查标签中的include标签
    然后开始使用不同的语言类型LanguageDriver进行真正的解析sql
    XMLScriptBuilder类：处理解析标签内容，开始处理我们自己的if,where等标签委派给不同的Handler进行处理，封装成不同的sqlNode
    然后根据sqlNode变成我们自己的sqlSource，然后再根据sqlSource变成MappedStatement





  *
  * */
}
