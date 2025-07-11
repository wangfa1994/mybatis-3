package com.wf.yeecode;

public class MainDemo {

  /*01 源码阅读*/
  /*02 mybatis概述*/
  /*03 mybatis运行初探----> com.wf.yeecode.a01.DemoApplication，运行代码查看流程 */

  /** 04 mybaits源码结构概述
   * 一共20个包，被分为三大类别：
   * 基础功能包(8)：这些包用来为其他包提供一些外围基础功能，例如文件读取功能，反射操作功能等，这些包的特点是功能相对独立，与业务逻辑耦合较小
   * annotations exceptions io lang logging  parsing reflection  type
   * 配置解析包(5)：这些包主要用来完成配置解析，存储等工作，这些包中的方法主要用在系统初始化阶段运行
   * binding builder mapping  scripting datasource
   * 核心操作包(7)：这些包用来完成数据库操作。在工作过程中，这些包可能会依赖基础功能包提供的基础功能和配置解析包提供的配置信息，这些包中的方法主要在数据库操作阶段运行
   * cache cursor executor jdbc plugin session transaction
   *  */


  /*a02 基础功能包源码阅读  basepackage */
  /** 05 exceptions包阅读
   * 主要分析了Java的异常体系结构，以及mybatis的包源码，定义了规范，各个真正的业务实现会散落到对应的业务包中，但是总的指导方针是在exceptions包中
  * */

  /**06 reflection包   com.wf.yeecode.basepackage.a02.A02ReflectionDemo
   *   factory包中定义了 对象工厂规范和默认的对象工厂实现，主要是用来创建对象的，mybatis中的对象创建都是通过这个来进行创建的
   *   ivoker包中主要是封装了针对getter setter属性的便捷获取和设置属性值，还有针对方法的便捷调用。Invoker为规范接口
   *   property包 主要是提供了一些便捷操作类，便于从类中解析对应的属性值，赋值对应的属性值
   *   wrapper包，主要是针对对象的包装，然后从对应的包装类中能方便的得到对应的属性，操作对应的属性等。
   *
   *   Reflector类 反射核心类，大集合，解析处理得到我们要反射的原始类，以及提供针对这个类的便捷操作。包括上述的相关操作，进行了组合
   *
   *   ParamNameResolver类 针对参数的解析处理
   *   TypeParameterResolver 类，主要操作方法的返回类型，请求类型等。
   *
   *
   *
   * */

  /** 07  annotations和 lang包 都是注解类   com.wf.yeecode.basepackage.a03.ParamDemo
   *   annotations包和 lang包 中存放的全是注解类， java中的注解类使用，以及分析如何激活触发这些注解类的使用
   *    @Param参数的解析主要是使用了 ParamNameResolver类进行处理
   *
  * */

  /** 08  type包  com.wf.yeecode.basepackage.a04type.TypeMain
   * 主要是一些类型转换  定义了三个类型处理器的标准规范 TypeHandler,然后进行了一个抽象类的实现BaseTypeHandler,
   * 然后真正的类型处理器，继承BaseTypeHandler开始处理对应的类型，虽然你定义实现了类型处理器，但是我使用者不知道你处理那个类型
   *
   * 于是添加了TypeReference抽象类，这个定义了一个方法，通过这个方法可以得到对应的Handler所处理的类型，这个类被BaseTypeHandler继承。
   *
   * 为了更好的使用，又提供了类型注册器，进行辅助使用。
   *
  * */

  /** 09  io包
   * 与磁盘文件的交互主要是对 xml配置文件的读操作。因此，io包中提供对磁盘文件读操作的支持。
   * 除了读取磁盘文件的功能外，io包还提供对内存中类文件（class文件）的操作
   *
   * 磁盘文件系统分为很多种，FAT,VFAT,NFS,NTFS等，不同文件系统的读写操作各不相同
   * VFS(Virtual File System)作为一个虚拟的文件系统将各个磁盘文件系统的差异屏蔽了起来，提供了统一的操作接口。
   *
   *在操作磁盘文件时，软件程序不需要和实体的文件系统打交道，只需要和VFS沟通即可，这使得系统的磁盘操作变得简单
   * VFS 抽象规范
   * DefaultVFS 和 JBoss6VFS 继承了规范
   *
   * 要把类文件加载成类，需要类加载器的支持 ClassLoad的封装 ClassLoaderWrapper
   *
   *
   * 10 logging包 日志包
   *
   *
   * 11 parsing 包  xml解析包
   *
   * */

  /* A03 配置解析包源码解读 */
   /**
   /**
   *
   *
   * 12 配置文件解析
   *MyBatis的配置依靠两类文件来完成，一类是配置文件，只有一个，主要包括mybatis的基本配置信息，另外一个是映射文件，这个文件配置java对象和数据库属性之间的映射关系,数据库操作语句等，可以有多个
   * 配置文件需要进行解析转换和保存，这些功能由多个类进行合作完成。
   *  配置文件的解析类：
   *
   * 13 binding包
   *  binding包是主要用来处理 Java方法与 SQL语句之间绑定关系的包
   *  1.维护映射接口中抽象方法和数据库曹总节点之间的关联关系
   *  2.为映射接口中的抽象方法接入对应的数据库操作
   *
   * Mybatis在初始化的时候hi进行各个映射文件的解析，将数据库的操作结点的信息记录到Configuration对啊ing中的mappedStatement属性中。
   *
   * MyBatis 还会在初始化阶段扫描所有的映射接口，并根据映射接口创建与之关联的MapperProxyFactory，两者的关联关系由 MapperRegistry 维护。
   * 当调用 MapperRegistry 的getMapper方法时，MapperProxyFactory会生产出一个 MapperProxy对象作为映射接口的代理。
   * SqlSession的getMapper方法最终也是调到MapperRegistry.getMapper中
   *
   *
   * 14 builder包
   * builder包是一个按照类型划分出来的包，包中存在许多的建造者类，
   *  也存在两个完善的功能
   *  1. 一是解析 XML配置文件和映射文件，这部分功能在 xml子包中
   *  2. 二是解析注解形式的 Mapper声明，这部分功能在 annotation子包中
   *
   * 15 mapping包
   *  定义了众多的解析实体类，这些实体类有的与sql语句相关，有的是与SQL的输入和输出参数有关，有的一些是与配置信息有关
    *  主要功能是
    *  sql语句处理功能
    *  输出结果处理功能
    *  输入参数处理功能
    *  多数据库种类处理功能
   *
   *
   *
   *
   *
   *
   *
   *
   *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    * *
  * */

}
