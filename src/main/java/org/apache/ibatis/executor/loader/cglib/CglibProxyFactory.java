/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.loader.cglib;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.ibatis.executor.loader.AbstractEnhancedDeserializationProxy;
import org.apache.ibatis.executor.loader.AbstractSerialStateHolder;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.loader.WriteReplaceInterface;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.session.Configuration;

/** 基于Cglib实现
 * @author Clinton Begin
 *
 * @deprecated Since 3.5.10, use Javassist instead.
 */
@Deprecated
public class CglibProxyFactory implements ProxyFactory {

  private static final String FINALIZE_METHOD = "finalize";
  private static final String WRITE_REPLACE_METHOD = "writeReplace";

  public CglibProxyFactory() {
    try {
      Resources.classForName("net.sf.cglib.proxy.Enhancer");
    } catch (Throwable e) {
      throw new IllegalStateException(
          "Cannot enable lazy loading because CGLIB is not available. Add CGLIB to your classpath.", e);
    }
  }

  @Override // 创建代理对象
  public Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration,
      ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, configuration, objectFactory,
        constructorArgTypes, constructorArgs); // 使用内部类进行创建代理对象，而且这个内部类还是一个methodInterceptor
  }
  // 创建一个反序列化的代理对象
  public Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
      ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return EnhancedDeserializationProxyImpl.createProxy(target, unloadedProperties, objectFactory, constructorArgTypes,
        constructorArgs);
  }

  static Object createStaticProxy(Class<?> type, Callback callback, List<Class<?>> constructorArgTypes,
      List<Object> constructorArgs) {
    LogHolder.log.warn("CglibProxyFactory is deprecated. Use another proxy factory implementation.");
    Enhancer enhancer = new Enhancer();
    enhancer.setCallback(callback);
    enhancer.setSuperclass(type);
    try {
      type.getDeclaredMethod(WRITE_REPLACE_METHOD);
      // ObjectOutputStream will call writeReplace of objects returned by writeReplace
      if (LogHolder.log.isDebugEnabled()) {
        LogHolder.log.debug(WRITE_REPLACE_METHOD + " method was found on bean " + type + ", make sure it returns this");
      }
    } catch (NoSuchMethodException e) {
      enhancer.setInterfaces(new Class[] { WriteReplaceInterface.class });
    } catch (SecurityException e) {
      // nothing to do here
    }
    Object enhanced;
    if (constructorArgTypes.isEmpty()) {
      enhanced = enhancer.create();
    } else {
      Class<?>[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
      Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
      enhanced = enhancer.create(typesArray, valuesArray);
    }
    return enhanced;
  }
  //实现了Cglib库中的MethodInterceptor，这个是cglib的回调接口
  private static class EnhancedResultObjectProxyImpl implements MethodInterceptor {

    private final Class<?> type; // 被代理的类
    private final ResultLoaderMap lazyLoader; // 要懒加载的属性的map[被代理对象可能会有多个属性可以被懒加载，尚未完成加载的属性就是在ResultMap存储]
    private final boolean aggressive; // 是否激进式的懒加载模式
    private final Set<String> lazyLoadTriggerMethods; // 能够触发全局懒加载的方法名称,初始化的equals", "clone", "hashCode", "toString"
    private final ObjectFactory objectFactory; // 对象工厂
    private final List<Class<?>> constructorArgTypes; // 被代理的类 的 构造函数的参数类型列表
    private final List<Object> constructorArgs; //被代理的类 的 构造函数的参数列表
    private final ReentrantLock lock = new ReentrantLock();

    private EnhancedResultObjectProxyImpl(Class<?> type, ResultLoaderMap lazyLoader, Configuration configuration,
        ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
      this.type = type;
      this.lazyLoader = lazyLoader;
      this.aggressive = configuration.isAggressiveLazyLoading();
      this.lazyLoadTriggerMethods = configuration.getLazyLoadTriggerMethods(); // 能够触发全局懒加载的方法名称的初始化加载
      this.objectFactory = objectFactory;
      this.constructorArgTypes = constructorArgTypes;
      this.constructorArgs = constructorArgs;
    }

    public static Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration,
        ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
      final Class<?> type = target.getClass();
      EnhancedResultObjectProxyImpl callback = new EnhancedResultObjectProxyImpl(type, lazyLoader, configuration,
          objectFactory, constructorArgTypes, constructorArgs);
      Object enhanced = createStaticProxy(type, callback, constructorArgTypes, constructorArgs);
      PropertyCopier.copyBeanProperties(type, target, enhanced);
      return enhanced;
    }

    @Override // 代理类的核心方法，当被代理类的方法被调用时，都会被拦截进入该方法 【finalize 和 writeReplace会被排除】 //参数列表 [代理对象本身，被调用的方法，被调用方法的参数，用来调用父类的代理]
    public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      final String methodName = method.getName(); //获得到此次被调用的方法的名称
      lock.lock();
      try {
        if (WRITE_REPLACE_METHOD.equals(methodName)) { // writeReplace方法序列化和反序列化中的方法
          Object original; // 调用writeReplace方法时，会创建原始对象
          if (constructorArgTypes.isEmpty()) {
            original = objectFactory.create(type);
          } else {
            original = objectFactory.create(type, constructorArgTypes, constructorArgs);
          }
          PropertyCopier.copyBeanProperties(type, enhanced, original);// 将被代理对象的属性拷贝到新创建的对象中
          if (lazyLoader.size() > 0) { // 如果存在懒加载属性
            return new CglibSerialStateHolder(original, lazyLoader.getProperties(), objectFactory, constructorArgTypes,
                constructorArgs); // 返回的对象，不仅仅是原对象，还有相关的懒加载的设置等信息。使用CglibSerialStateHolder进行封装
          } else {
            return original; // 不存在懒加载属性的话，就直接返回了延时对象
          }
        } // 排除writeReplace方法 并且排除了finalize方法
        if (lazyLoader.size() > 0 && !FINALIZE_METHOD.equals(methodName)) { //存在懒加载属性并且被调用的不是finalize方法
          if (aggressive || lazyLoadTriggerMethods.contains(methodName)) { // 设置了激进懒加载或者被调用的方法是能够触发全局加载的方法
            lazyLoader.loadAll(); // 完成所有属性的懒加载
          } else if (PropertyNamer.isSetter(methodName)) { // 调用了属性的写方法，先进行清除该属性的懒加载设置，该属性不需要被加载了
            final String property = PropertyNamer.methodToProperty(methodName); //
            lazyLoader.remove(property);
          } else if (PropertyNamer.isGetter(methodName)) { // 调用了属性的读方法
            final String property = PropertyNamer.methodToProperty(methodName);
            if (lazyLoader.hasLoader(property)) { // 如果还没有进行懒加载，则进行懒加载处理
              lazyLoader.load(property);
            }
          }
        } // 触发被代理类的相应方法，能够进行到这里的是除去writeReplace方法外的方法，读写方法，toString方法等
        return methodProxy.invokeSuper(enhanced, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      } finally {
        lock.unlock();
      } // 方法流程：如果设置了激进懒加载或者被调用的是触发全局加载的方法，则直接加载所有未加载的属性
    }// 如果被调用的是属性的写方法，则将该方法从懒加载列表中删除，因为此时数据库中的数据已经不是最新的了，没有必要再去加载，然后进行属性的写入操作
  }// 如果被调用的是属性的读方法，且该属性尚未被懒加载的哈，则加载该属性，如果该属性已经被懒加载过，直接进行读取该属性

  private static class EnhancedDeserializationProxyImpl extends AbstractEnhancedDeserializationProxy
      implements MethodInterceptor {

    private EnhancedDeserializationProxyImpl(Class<?> type, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
        ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
      super(type, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
    }

    public static Object createProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
        ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
      final Class<?> type = target.getClass();
      EnhancedDeserializationProxyImpl callback = new EnhancedDeserializationProxyImpl(type, unloadedProperties,
          objectFactory, constructorArgTypes, constructorArgs);
      Object enhanced = createStaticProxy(type, callback, constructorArgTypes, constructorArgs);
      PropertyCopier.copyBeanProperties(type, target, enhanced);
      return enhanced;
    }

    @Override
    public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      final Object o = super.invoke(enhanced, method, args);
      return o instanceof AbstractSerialStateHolder ? o : methodProxy.invokeSuper(o, args);
    }

    @Override
    protected AbstractSerialStateHolder newSerialStateHolder(Object userBean,
        Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
        List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
      return new CglibSerialStateHolder(userBean, unloadedProperties, objectFactory, constructorArgTypes,
          constructorArgs);
    }
  }

  private static class LogHolder {
    private static final Log log = LogFactory.getLog(CglibProxyFactory.class);
  }

}
