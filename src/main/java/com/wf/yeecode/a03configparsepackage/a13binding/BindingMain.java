package com.wf.yeecode.a03configparsepackage.a13binding;

public class BindingMain {

  /*
  binding包基于反射进行实现功能的，将抽象方法接入实现方法 ，主要用于处理Java方法和SQL语句之间的绑定关系
  通过接口调用可以直接找到对应的SQL,

  功能一：为映射接口中的抽象方法接入对应的数据库操作是相对底层的操作【将接口与映射文件进行绑定】
      MapperProxy类 实现 InvocationHandler 是jdk动态代理的一个实现

       MapperProxyFactory类 产生对应的MapperProxy

       MapperMethod 映射的方法类 会包括ParamMap ,SqlCommand , MethodSignature

       MapperRegistry 注册类 进行注册我们的mapperProxyFactory


      想要将数据库库操作接入到抽象方法中，代理类少不了的，并且我们需要将数据库的操作结点转化为一个方法。
      MapperMethod对象就表示数据库操作转化后的方法,MapperMethod实例中的execute方法触发节点中的sql语句

      MapperMethod 类 将一个数据库操作语句和一个Java方法绑定在一起，他的MethodSignature属性保存了这个方法的详细信息
      sqlCommand属性保存了这个方法对应的sql语句。


      对接方法的操作<----MethodSignature   SqlCommand --->对接数据库的操作

    通过MapperMethod类的帮助，我们将Java的接口调用转换为MapperMethod对象execute方法的调用，这样的话，就能在调用Java接口的时候完成针对数据库的操作

    功能二：数据库操作方法的接入
    我们怎么才能调用接口触发到对应的MapperMethod的execute方法上的调用呢？
    接口方法的调用，会找到对应的实现类进行完成，但是我们所有的映射接口并没有进行实现，这样的话，我们只能想办法在接口调用的时候转到一个通用的代理类上，
    这样的话，所有的接口方法调用都转到这个代理类上，通过这个代理类进行调用MapperMethod的execute方法。
    MapperProxy继承了InvocationHandler接口，是一个动态代理类，对被代理对象方法的调用都会被转到MapperProxy中的Invocation中的invoke上。
    我们需要一个代理类，在这个代理类中进行MapperProxy的调用。
    MapperProxyFactory是生产MapperProxy的工厂，调用newInstance就会产生一个MapperProxy对象，通过MapperProxy的invoke方法的调用转到我们的MapperMethod中的execute调用

    jdk 在创建代理对象的时候会调用 Proxy.newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
    我们的接口是很多的，而且我们不知道业务什么时候触发这个接口方法的调用，所以我们需要先收集所有的接口，然后在需要的时候尽心产生代理对象，

    【映射接口.java的文件特别多，一个映射文件会对应一个映射文件，接口中的方法又很多，一个方法会对应一个MapperMethod,一个mapperMethod又绑定一个映射文件中的sql这个关系要怎么维护：】
    【1.先將映射接口与MapperProxyFactory关联起来这种关系维护在MapperRegistry类中的knownMappers属性中，一个mapper文件对应一个MapperProxyFactory，然后通过我们的MapperRegistry类
    将我们的映射接口和MapperProxyFactory进行绑定】
    【2.将Java方法method与对应的MapperMethod进行绑定，这个是在MapperProxy中的methodCache中进行维护的，但是怎么放进去的呢？】








  *
  * */
}
