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
package org.apache.ibatis.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
// 参数名称解析器 ，主要是用来解析对应的mapper接口中的方法中的参数信息，构造器的时候进行相关信息初始化，然后将实际参数传递进来得到形参和实参的对应关系
public class ParamNameResolver {

  public static final String GENERIC_NAME_PREFIX = "param";

  public static final String[] GENERIC_NAME_CACHE = new String[10]; // 默认了一个方法的最大参数个数为10个。进行先缓存param1,param2,param3...param10

  static {
    for (int i = 0; i < 10; i++) {
      GENERIC_NAME_CACHE[i] = GENERIC_NAME_PREFIX + (i + 1);
    }
  }
  // 是否使用了真实参数标签的名称，如果使用了的话，会进行处理没有@param进行标注的参数的解析
  private final boolean useActualParamName;

  /**
   * <p>
   * The key is the index and the value is the name of the parameter.<br />
   * The name is obtained from {@link Param} if specified. When {@link Param} is not specified, the parameter index is
   * used. Note that this index could be different from the actual index when the method has special parameters (i.e.
   * {@link RowBounds} or {@link ResultHandler}).
   * </p>
   * <ul>
   * <li>aMethod(@Param("M") int a, @Param("N") int b) -&gt; {{0, "M"}, {1, "N"}}</li>
   * <li>aMethod(int a, int b) -&gt; {{0, "0"}, {1, "1"}}</li>
   * <li>aMethod(int a, RowBounds rb, int b) -&gt; {{0, "0"}, {2, "1"}}</li>
   * </ul>
   */  // 方法输入参数的参数次序表，key为参数次序，value为参数名称后者参数@Param注解的值
  private final SortedMap<Integer, String> names;
  // 该方法输入的参数中是否含有@Param注解
  private boolean hasParamAnnotation;

  public ParamNameResolver(Configuration config, Method method) { // 构造器方法，丢进去了一个全局的配置文件，还有一个方法
    this.useActualParamName = config.isUseActualParamName();
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Annotation[][] paramAnnotations = method.getParameterAnnotations(); // 得到方法上的参数所有的注解，是一个二维数组，一个参数上可能有多个注解 ，jdk的方法
    final SortedMap<Integer, String> map = new TreeMap<>();
    int paramCount = paramAnnotations.length; // 参数个数
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      if (isSpecialParameter(paramTypes[paramIndex])) { // 跳过方法上特殊的参数，包括两个RowBounds，ResultHandler
        // skip special parameters
        continue;
      }
      String name = null;
      for (Annotation annotation : paramAnnotations[paramIndex]) { // 循环处理参数上的注解列表得到我们的parameter注解 paramAnnotation(@Param("name")@Mapper String name)
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          name = ((Param) annotation).value(); // 获得到我们的Param值
          break;
        }
      }
      if (name == null) {
        // @Param was not specified.没有指定@Param。
        if (useActualParamName) { //没有指定@Param ,根据是否需要进行保留参数的原有名称
          name = getActualParamName(method, paramIndex);
        }
        if (name == null) { // 参数名称获取不到的情况下，按照参数index命名
          // use the parameter index as the name ("0", "1", ...)
          // gcode issue #71
          name = String.valueOf(map.size());
        }
      }
      map.put(paramIndex, name); // 将索引值和我们的param值进行绑定
    }
    names = Collections.unmodifiableSortedMap(map); // 放入到我们ParamNameResolver中的names属性中
  }

  private String getActualParamName(Method method, int paramIndex) {
    return ParamNameUtil.getParamNames(method).get(paramIndex);
  }

  private static boolean isSpecialParameter(Class<?> clazz) {
    return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
  }

  /** 返回由SQL提供程序引用的参数名。
   * Returns parameter names referenced by SQL providers.
   *
   * @return the names
   */
  public String[] getNames() {
    return names.values().toArray(new String[0]);
  }

  /**
   * <p> 返回一个没有名称的非特殊参数
   * A single non-special parameter is returned without a name. Multiple parameters are named using the naming rule. In
   * addition to the default names, this method also adds the generic names (param1, param2, ...).
   * </p>
   *
   * @param args
   *          the args
   *
   * @return the named params
   */
  public Object getNamedParams(Object[] args) { // 将我们的实参和形参进行绑定 ,返回的是一个map对象，不仅进行了参数名的对应，还进行了param1.param2.param3...的对应，防止xml中写入了param
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
      return null;
    }
    if (!hasParamAnnotation && paramCount == 1) { // 如果只有一个参数，并且参数上没有进行绑定@Param注解
      Object value = args[names.firstKey()];
      return wrapToMapIfCollection(value, useActualParamName ? names.get(names.firstKey()) : null); //  进行包装实参，此时一定返回的是一个对象
    } else {
      final Map<String, Object> param = new ParamMap<>(); // 如果存在多个参数的话，直接返回Method内部类ParamMap
      int i = 0;
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
        param.put(entry.getValue(), args[entry.getKey()]);// 封装成第一个的value为key，得到第二个的key的值
        // add generic param names (param1, param2, ...) 添加通用参数名（param1, param2，…）
        final String genericParamName = i < 10 ? GENERIC_NAME_CACHE[i] : GENERIC_NAME_PREFIX + (i + 1); // 缓存了10个大小的param1 param2 param3...
        // ensure not to overwrite parameter named with @Param  确保不要覆盖以@Param命名的参数
        if (!names.containsValue(genericParamName)) {
          param.put(genericParamName, args[entry.getKey()]); //将实参也赋值给对应的param1 param2 param3上，用来防止xml中使用了param1，param1等
        }
        i++;
      }
      return param;
    }
  }

  /**
   * Wrap to a {@link ParamMap} if object is {@link Collection} or array.
   *
   * @param object
   *          a parameter object
   * @param actualParamName
   *          an actual parameter name (If specify a name, set an object to {@link ParamMap} with specified name)
   *
   * @return a {@link ParamMap}
   *
   * @since 3.5.5
   */
  public static Object wrapToMapIfCollection(Object object, String actualParamName) {
    if (object instanceof Collection) { //是否是 Collection
      ParamMap<Object> map = new ParamMap<>();
      map.put("collection", object);
      if (object instanceof List) {
        map.put("list", object);
      }
      Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
      return map;
    }
    if (object != null && object.getClass().isArray()) { // 是否是数组
      ParamMap<Object> map = new ParamMap<>();
      map.put("array", object);
      Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
      return map;
    }
    return object; //都不是的话，直接原样返回吧
  }

}
