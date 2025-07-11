package com.wf.yeecode.a03configparsepackage.a12;

public class ConfigMain {
  /*
   配置类的解析
   首先完成针对配置文件和映射文件的解析，将配置文件中的信息进行提取，转换，最终保存在Java对象中，根据解析出来的信息设置好mybatis的运行时环境
   解析器类：用来解析我们的配置功能，负责完成信息的提取和转换，XMLConfigBuilder,XMLMapperBuilder,XMLStatementBuilder,CacheRefResolver类等
   解析实体类：提供配置类的保存功能，这些类在结构上和配置文件中的信息存在对应关系，配置文件解析出来的信息最终会保存到解析实体类的属性中。Configuration，ReflectFactory，Environment,Datasource,ParameterMap,ParameterMapping,Discriminator,SqlNode等
   对应关系参考 config-entity.xml  mapper-entity.xml

   阅读配置解析类源码:
   从类的角度分析，将源码中的解析器类和解析实体类划分出
   从配置文件的角度分析，将各个配置信息对应的解析器类和解析实体类找出来



  * */
}
