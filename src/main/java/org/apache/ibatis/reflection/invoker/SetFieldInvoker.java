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
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * @author Clinton Begin
 */
public class SetFieldInvoker implements Invoker {
  private final Field field; // 设置属性值操作，需要知道那个属性，通过构造器传入

  public SetFieldInvoker(Field field) {
    this.field = field;
  }

  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException {
    try {
      field.set(target, args[0]); // 通过反射直接设置目标对象的属性值
    } catch (IllegalAccessException e) {
      if (!Reflector.canControlMemberAccessible()) {
        throw e;
      }
      field.setAccessible(true);
      field.set(target, args[0]);
    }
    return null;
  }

  @Override
  public Class<?> getType() {
    return field.getType();
  }
}
