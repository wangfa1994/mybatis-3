/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.util.MapUtil;

/** 我们的代理对象，通过代理对象的调用，转到我们的MapperMethodInvoker中的MapperMethod中执行我们的sql
 * @author Clinton Begin
 * @author Eduardo Macarron  MapperProxy类对应的是映射文件，通过MapperRegistry将映射接口和映射文件的对应关系建立起来
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -4724728412955527868L;
  private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
      | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;
  private static final Constructor<Lookup> lookupConstructor;
  private static final Method privateLookupInMethod;
  private final SqlSession sqlSession; // 我们从sqlSessionFactory中得到的sqlSession,可以处理对应的增删改查语句
  private final Class<T> mapperInterface; // T 我们的接口类型，代理的接口，
  private final Map<Method, MapperMethodInvoker> methodCache; // 缓存我们的方法和对应的MapperMethod，完成MapperProxy与MapperMethod的绑定，这个Method 和 MapperMethod是怎么绑定的

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  static {
    Method privateLookupIn;
    try {
      privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
    } catch (NoSuchMethodException e) {
      privateLookupIn = null;
    }
    privateLookupInMethod = privateLookupIn;

    Constructor<Lookup> lookup = null;
    if (privateLookupInMethod == null) {
      // JDK 1.8
      try {
        lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        lookup.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
            e);
      } catch (Exception e) {
        lookup = null;
      }
    }
    lookupConstructor = lookup;
  }
  // proxy 代理对象  method 代理方法  args 代理方法参数
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) { // 这个方法是来自哪个类，如果来自Object 直接进行调用
        return method.invoke(this, args);
      } // 进行method的调用，这样的话，就能转到对应的MapperMethod中的execute方法的调用
      return cachedInvoker(method).invoke(proxy, method, args, sqlSession); // 在cachedInvoker中会进行创建MapperMethod封装成PlainMethodInvoker然后委托调用
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
    try { // 如果我们的 methodCache中没有key为method的缓存对象，我们需要创建一个MapperMethodInvoker
      return MapUtil.computeIfAbsent(methodCache, method, m -> {
        if (!m.isDefault()) { // 如果我们的method是默认方法的返回对象 PlainMethodInvoker
          return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
        }
        try {
          if (privateLookupInMethod == null) { // 返回不同的jdk版本的invoker
            return new DefaultMethodInvoker(getMethodHandleJava8(method));
          }
          return new DefaultMethodInvoker(getMethodHandleJava9(method));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException
            | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException re) {
      Throwable cause = re.getCause();
      throw cause == null ? re : cause;
    }
  }

  private MethodHandle getMethodHandleJava9(Method method)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Class<?> declaringClass = method.getDeclaringClass();
    return ((Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup())).findSpecial(
        declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
        declaringClass);
  }

  private MethodHandle getMethodHandleJava8(Method method)
      throws IllegalAccessException, InstantiationException, InvocationTargetException {
    final Class<?> declaringClass = method.getDeclaringClass();
    return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
  }

  interface MapperMethodInvoker {
    Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
  }
  // 上面是接口定义分为两个 一个是普通方法调用器， 一个是默认方法调用器
  private static class PlainMethodInvoker implements MapperMethodInvoker {
    private final MapperMethod mapperMethod;

    public PlainMethodInvoker(MapperMethod mapperMethod) {
      this.mapperMethod = mapperMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      return mapperMethod.execute(sqlSession, args); // 进行委托调用，转到MapperMethod中
    }
  }
  // 默认方法的调用器
  private static class DefaultMethodInvoker implements MapperMethodInvoker {
    private final MethodHandle methodHandle;

    public DefaultMethodInvoker(MethodHandle methodHandle) {
      this.methodHandle = methodHandle;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      return methodHandle.bindTo(proxy).invokeWithArguments(args);  // 进行委托调用，转到MapperMethod中
    }
  }
}
