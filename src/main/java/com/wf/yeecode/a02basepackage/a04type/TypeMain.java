package com.wf.yeecode.a02basepackage.a04type;

public class TypeMain {

  /** 类型处理器
   *  TypeHandler 接口 顶级接口  主要从我们的sql结果中获取到对应的类型，或者是给我们的sql设置我们指定的参数
   *  BaseTypeHandler  是顶级接口的一个抽象实现 并且继承了类型参考器
   *  TypeReference  类型参考器  选用哪个TypeHandler进行设置或者得到值呢，通过我们的TypeReference进行得到，能够判断出一个TypeHandler用来处理的目标类型，她被BaseTypeHandler锁继承了
   *  *TypeHandler 类型处理器(43个)
   *
   *
   * 定义了大量的类型处理器，但是我们怎么便捷的进行使用，遇到某种类型时能快速的找到与数据的类型相匹配的类型处理器，这个就是类型注册表
   *
   * 类型注册表
   *  SimpleTypeRegistry 基本类型注册表，使用set集合维护了所有Java基本数据类型的集合，并且可以判断是否是属于基本类型
   *  TypeAliasRegistry 类型别名注册表 使用hashMap维护所有类型的别名和类型的映射关系
   *  TypeHandlerRegistry  类型处理器的注册表 维护了所有类型和对应类型处理器的映射关系，数据类型和相关处理器的对应关系由这个进行维护。上面的两个进行辅助
   *
   *  注解类
   *  @Alias
   *  @MappedJdbcTypes
   *  @MappedTypes
   *
   *  异常类
   *  TypeException
   *
   *  工具类
   *  ByteArrayUtils 数组转换的工具
   *
   *  枚举类
   *  JdbcType  定义了 所有的jdbc类型，类型来自于 java.sql.Types
   *
   *
   *
   *
   *
   * Java数据类型 和 JDBC数据类型
   * java中的数据类型和jdbc数据类型的关系可不是一一对应的，因为java中的String name，可能会对应数据库中的char,varchar,text等多种类型
   *
   *
   *
   *
   * */
}
