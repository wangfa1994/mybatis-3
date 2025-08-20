package com.wf.yeecode.a04corepackage.a24plugin;

public class PluginMain {

  /**
   * MyBatis还提供插件功能 plugin功能
   *
   * 责任链模式
   * 一个目标对象可能需要经过多个对象的处理 ，责任链模式将多个处理器组装成一个链条，被处理对象被放置到链条的起始端后，会自动在整个链条上传递和处理
   *
   *责任链模式不仅降低了被处理对象和处理器之间的耦合度，还使得我们可以更为灵活地组建处理过程。例如，我们可以很方便地向责任链中增、删处理器或者调整处理器的顺序
   *
   *
   * @Intercepts({ @Signature(type = ResultSetHandler.class, method = "handResultSets", args = {Statement.class}) })
   * 拦截器类上有注解 Intercepts，Intercepts的参数是 Signature注解数组。每个 Signature注解都声明了当前拦截器类要拦截的方法
   * type：拦截器要拦截的类型，
   * method：拦截器要拦截的type类型中的方法
   * args：拦截器要拦截的type类型中的method方法的参数类型列表
   *
   * Interceptor接口中有三个方法供拦截器类实现
   *  intercept方法：拦截器类必须实现该方法。拦截器拦截到目标方法时，会将操作转接到该 intercept 方法上，其中的参数 Invocation 为拦截到的目标方法
   *  plugin方法： 输出一个对象来替换输入参数传入的目标对象，在默认实现中，会调用 Plugin.wrap方法给出一个原有对象的包装对象，然后用该对象来替换原有对象
   *  setProperties方法：为拦截器设置属性
   *
   *
   *  plugin包搭建了一个拦截器平台
   *  Plugin 类 继承了 java.lang.reflect.InvocationHandler接口，因此是一个基于反射实现的代理类
   *  Plugin类完成了类层级和方法层级这两个层级的过滤工作.
   *  1.如果目标对象所属的类被拦截器声明拦截，则 Plugin用自身实例作为代理对象替换目标对象
   *  2.如果目标对象被调用的方法被拦截器声明拦截，则Plugin将该方法交给拦截器处理。否则 Plugin将该方法交给目标对象处理
   *  正因为 Plugin类完成了大量的工作，拦截器自身所需要做的工作就非常简单了，主要分为两项：
   *  使用 Intercepts 注解和 Signature注解声明自身要拦截的类型与方法；通过intercept方法处理被拦截的方法。
   *
   *拦截器是通过替换目标对象实现的,基于 Plugin类，使用动态代理对象替换目标对象,MyBatis 中一共只有四个类的对象可以被拦截器
   * 替换，它们分别是ParameterHandler、ResultSetHandler、StatementHandler 和 Executor。而且替换只能发生在固定的地方，我们称其为拦截点
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
   */
  public static void main(String[] args) {

  }
}
