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
package org.apache.ibatis.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** 类型参考器
 * References a generic type.
 *
 * @param <T>
 *          the referenced type
 *
 * @since 3.1.0
 *
 * @author Simone Tripodi
 */
public abstract class TypeReference<T> {

  private final Type rawType;  // Type 是Jdk中的数据 ，这个属性会被所有的TypeHandler拥有，表明自己能处理的目标类型

  protected TypeReference() { // 这个被设计成抽象类，感觉怪怪的，但是这个功能又和 TypeHandler分开了
    rawType = getSuperclassTypeParameter(getClass());
  }
  // 因为所有的TypeHandler都继承此类，所以可以通过这个方法解析出当前TypeHandler实现类能够处理的目标类型，
  Type getSuperclassTypeParameter(Class<?> clazz) { // 通过我们传递的Class,解析得到我们的Type类型，进而判断出我们所需要的事那个TypeHandler
    Type genericSuperclass = clazz.getGenericSuperclass(); // 获得class类上的带有泛型的直接父类
    if (genericSuperclass instanceof Class) {
      // try to climb up the hierarchy until meet something useful
      if (TypeReference.class != genericSuperclass) {
        return getSuperclassTypeParameter(clazz.getSuperclass());
      }
      // 说明实现类虽然实现了TypeReference类，但是没有进行泛型的使用
      throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. "
          + "Remove the extension or add a type parameter to it.");
    }
    // genericSuperclass为我们的实现使用了泛型类，获取泛型类的第一个参数 T
    Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    // TODO remove this when Reflector is fixed to return Types
    if (rawType instanceof ParameterizedType) { //如果是参数化类型，获取参数化类型的实际类型
      rawType = ((ParameterizedType) rawType).getRawType();
    }

    return rawType;
  }

  public final Type getRawType() {
    return rawType;
  }

  @Override
  public String toString() {
    return rawType.toString();
  }

}
