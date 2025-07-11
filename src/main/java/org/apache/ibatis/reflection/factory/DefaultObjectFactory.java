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
package org.apache.ibatis.reflection.factory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.Reflector;

/**
 * @author Clinton Begin
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {

  private static final long serialVersionUID = -8855120656740914948L;

  @Override
  public <T> T create(Class<T> type) {
    return create(type, null, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    Class<?> classToCreate = resolveInterface(type); // 先进行判断给定的类型是否是接口类型，如果是的话，则返回一个符合改接口实现的类
    // we know types are assignable
    return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
  }

  private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    try {
      Constructor<T> constructor;
      if (constructorArgTypes == null || constructorArgs == null) { // 如果参数类型列表和参数列表为null 直接使用无惨构造器
        constructor = type.getDeclaredConstructor();
        try {
          return constructor.newInstance(); // 使用无惨构造器进行创建对象
        } catch (IllegalAccessException e) {
          if (Reflector.canControlMemberAccessible()) {
            constructor.setAccessible(true); // 创建失败，再进行一次设置创建
            return constructor.newInstance();
          }
          throw e;
        }
      }
      constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0])); // 根据输入参数类型查找指定的构造器
      try {
        return constructor.newInstance(constructorArgs.toArray(new Object[0]));
      } catch (IllegalAccessException e) {
        if (Reflector.canControlMemberAccessible()) {
          constructor.setAccessible(true);
          return constructor.newInstance(constructorArgs.toArray(new Object[0]));
        }
        throw e;
      }
    } catch (Exception e) {
      String argTypes = Optional.ofNullable(constructorArgTypes).orElseGet(Collections::emptyList).stream()
          .map(Class::getSimpleName).collect(Collectors.joining(",")); // 收集所有的参数类型
      String argValues = Optional.ofNullable(constructorArgs).orElseGet(Collections::emptyList).stream()
          .map(String::valueOf).collect(Collectors.joining(",")); // 收集所有的参数
      throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes + ") or values ("
          + argValues + "). Cause: " + e, e);// 抛出异常
    }
  }

  protected Class<?> resolveInterface(Class<?> type) {
    Class<?> classToCreate;
    if (type == List.class || type == Collection.class || type == Iterable.class) {
      classToCreate = ArrayList.class;
    } else if (type == Map.class) {
      classToCreate = HashMap.class;
    } else if (type == SortedSet.class) { // issue #510 Collections Support
      classToCreate = TreeSet.class;
    } else if (type == Set.class) {
      classToCreate = HashSet.class;
    } else {
      classToCreate = type; //如果不是jdk的还原样返回
    }
    return classToCreate;
  }

  @Override
  public <T> boolean isCollection(Class<T> type) {
    return Collection.class.isAssignableFrom(type); // 判断某一个类型是否来自Collection
  }

}
