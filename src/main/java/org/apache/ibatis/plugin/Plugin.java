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
package org.apache.ibatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.util.MapUtil;

/** 插件核心类 基于反射完成的代理类 , Plugin类完成类层级和方法层级这两个层级的过滤工作
 * @author Clinton Begin
 */
public class Plugin implements InvocationHandler {

  private final Object target; // 被代理的对象
  private final Interceptor interceptor; // 拦截器
  private final Map<Class<?>, Set<Method>> signatureMap; // 拦截器要拦截的所有的类，以及类中的方法，通过 getSignatureMap方法从拦截器的 Intercepts注解和 Signature注解中获取的

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }
  //根据拦截器配置来生成一个对象用来替换被代理对象 [被代理对象，拦截器]
  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor); //通过方法得到拦截器Interceptor要拦截的类型与方法
    Class<?> type = target.getClass();//被代理对象的类型
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);//逐级寻找对代理对象类型的父类，将父类中需要被拦截的全部找出
    if (interfaces.length > 0) { //创建并返回一个代理对象，代理逻辑为Plugin类
      return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
    }
    return target; //直接返回原有被代理对象，这意味着被代理对象的方法不需要被拦截
  } // 如果一个目标类需要被某个拦截器拦截的话，那么这个类的对象已经在 warp方法中被替换成了代理对象，即 Plugin对象,当目标类的方法被触发时，会直接进入 Plugin对象的 invoke方法
  // invoke方法中，会进行方法层面的进一步判断,如果拦截器声明了要拦截此方法，则将此方法交给拦截器执行,如果拦截器未声明拦截此方法，则将此方法交给被代理对象完成.
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { //[代理对象，代理方法，代理方法的参数]
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass()); // 获得该类所有需要被拦截的方法
      if (methods != null && methods.contains(method)) { //判断方法是否需要被拦截器拦截，如果需要，则需要交给拦截器处理
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args); //该方法不需要被拦截，交给被代理对象处理
    } catch (Exception e) { //
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }
  // 获取拦截器要拦截的所有类和类中的方法 [拦截器]
  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class); //获得拦截器的注解
    // issue #251
    if (interceptsAnnotation == null) {
      throw new PluginException(
          "No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
    } // @Intercepts({ @Signature(type = ResultSetHandler.class, method = "handResultSets", args = {Statement.class}) })
    Signature[] sigs = interceptsAnnotation.value(); // 得到拦截器的Signature属性
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
    for (Signature sig : sigs) { // 循环遍历Signature属性,放入map中， type为key，方法为value
      Set<Method> methods = MapUtil.computeIfAbsent(signatureMap, sig.type(), k -> new HashSet<>());
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e,
            e);
      }
    }
    return signatureMap;
  } // 进行收集了拦截器的类型信息之后，Plugin就可以判断当前的类型是否需要被拦截器拦截了，如果需要被拦截就会创建代理类wrap方法处理

  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[0]);
  }

}
