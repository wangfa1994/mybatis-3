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
package org.apache.ibatis.builder.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.BuilderException;

/** 通过SQL提供程序类解析SQL提供程序方法的接口。 只有一个默认方法，
 * The interface that resolve an SQL provider method via an SQL provider class.
 * <p>
 * This interface need to implements at an SQL provider class and it need to define the default constructor for creating
 * a new instance.
 *
 * @since 3.5.1
 *
 * @author Kazuki Shimizu
 */
public interface ProviderMethodResolver {

  /**
   * Resolve an SQL provider method.
   * <p>
   * The default implementation return a method that matches following conditions.
   * <ul>
   * <li>Method name matches with mapper method</li>
   * <li>Return type matches the {@link CharSequence}({@link String}, {@link StringBuilder}, etc...)</li>
   * </ul>
   * If matched method is zero or multiple, it throws a {@link BuilderException}.
   *
   * @param context
   *          a context for SQL provider
   *
   * @return an SQL provider method
   *
   * @throws BuilderException
   *           Throws when cannot resolve a target method
   */
  default Method resolveMethod(ProviderContext context) { // 从带Provider的注解中的type属性指向的类中找出method属性指定的方法
    List<Method> sameNameMethods = Arrays.stream(getClass().getMethods()) //找出同名方法
        .filter(m -> m.getName().equals(context.getMapperMethod().getName())).collect(Collectors.toList());
    if (sameNameMethods.isEmpty()) { // 没有找到方法，进行抛出异常
      throw new BuilderException("Cannot resolve the provider method because '" + context.getMapperMethod().getName()
          + "' not found in SqlProvider '" + getClass().getName() + "'.");
    }
    List<Method> targetMethods = sameNameMethods.stream() //根据返回类型再次判断，返回类型必须是CharSequence类型及其子类
        .filter(m -> CharSequence.class.isAssignableFrom(m.getReturnType())).collect(Collectors.toList());
    if (targetMethods.size() == 1) {
      return targetMethods.get(0); // 方法唯一进行返回
    } // 第一步先找出符合方法名的所有方法；第二步根据方法的返回值进行进一步校验
    if (targetMethods.isEmpty()) {
      throw new BuilderException("Cannot resolve the provider method because '" + context.getMapperMethod().getName()
          + "' does not return the CharSequence or its subclass in SqlProvider '" + getClass().getName() + "'.");
    }
    throw new BuilderException("Cannot resolve the provider method because '" + context.getMapperMethod().getName()
        + "' is found multiple in SqlProvider '" + getClass().getName() + "'.");
  }

}
