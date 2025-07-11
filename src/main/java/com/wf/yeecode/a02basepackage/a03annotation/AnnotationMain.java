package com.wf.yeecode.a02basepackage.a03annotation;

public class AnnotationMain {
  /** java 注解是一种标注，类，方法，变量，参数 包等均可以被注解标注，从而添加额外的信息。相比于直接修改代码，更松耦合
   Java包括五个元注解 java.lang.annotation.Annotation
   @Documented 不需要设置具体的值。如果一个注解被@Documented标注，则该注解会在 javadoc中生成

   @Retention(RetentionPolicy.RUNTIME) 用来声明注解的声明周期，表明注解会被保留到哪一个阶段，源代码阶段()，编译阶段，运行期间
      SOURCE：保留到源代码阶段。这一类注解一般留给编译器使用，在编译时会被擦除
      CLASS：保留到类文件阶段。这是默认的生命周期，注解会保留到类文件阶段，但是 JVM运行时不包含这些信息
      RUNTIME: 保留到 JVM运行阶段。如果想在程序运行时获得注解，则需要保留在这一阶段

   @Target(ElementType.ANNOTATION_TYPE)  用来声明注解可以用在什么地方 ，类上 ，方法上，字段上等 ElementType

   @Inherited 不需要设置具体的值。如果一个注解被@Inherited标注，表明允许子类继承父类的该注解，但是不能从接口继承该注解

   @Repeatable(ConstructorArgs.class) 如果一个注解被@Repeatable标注，则该注解可以在同一个地方被重复使用多次。用@Repeatable来修饰注解时需要指明一个接受重复注解的容器


   @Param注解的生效 与 使用

  * */
}
