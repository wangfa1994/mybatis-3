package com.wf.yeecode.a02basepackage.a06log;

public class LogMain {

  /* java的日志发展体系
  java发展初期是没有日志体系的，只能依赖于System.out进行打印到控制台，无法打印到其他位置，并且是同步的，对性能有影响
   缺点 无法禁用，无法分级，没有线程名，时间戳，类名等相关信息

   2001年产生了日志实现框架 log4j，首次引入三个核心【日志记录器Logger，输出目的地Appender，日志布局Layout】
   通过外部的配置文件 log4j.properties或者log4j.xml进行配置文件的加载

   2002年官方入场，1.4版本进行定义了JUL,java.util.logging,想要制定标准日志实现，但是拉胯，性能和便捷性上都不如log4j,
   它的引入，混淆了日志框架的使用，不得不进行相互适配。开发者需要同时面对log4j和jul

   同年2002年为了统计日志和方便使用，进行第一次统一的尝试，产生了JCL (Jakarta Commons Logging)，门面模式诞生，
   他只提供了一套统一的的日志接口[org.apache.commons.logging.Log]，不提供实现，只需要依赖这个接口，在运行时，JCL
   会通过一套复杂的动态发现机制（使用自定义的类加载器）在 Classpath 中寻找实际的日志实现（Log4j/JUL），然后将日志调用委派给它
   应用程序可以从具体的日志实现中解脱出来。理论上，只需更换 Classpath 中的日志实现 JAR 包，就可以切换日志框架，但是存在
   致命的弱点 【类加载器地狱” (ClassLoader Hell)：JCL 的动态发现机制在复杂的应用环境（如 Tomcat、OSGi）中非常脆弱，
   经常导致 ClassNotFoundException 或 NoClassDefFoundError 等难以诊断的问题。】

   2006年产生了一套标准slf4j+实现logBack，log4j的原始作者，重新发布了SLF4J (Simple Logging Facade for Java)，用来解决优化JCL门面，产生了新的门面。并且还
   进行实现了一套标准Logback日志实现框架。 slf4j是一个接口规范，只是定义了标准，而后续的Logback则是其标准实现， 然后还提供了一
   系列标准的桥接包，用于将老的日志体系(log4j,JUL,JCL)桥接到slf4j上

    2012年









  *
  * */
}
