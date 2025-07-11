/*
 *    Copyright 2009-2022 the original author or authors.
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

import java.lang.reflect.InvocationTargetException;

/**
 * @author Clinton Begin
 */
public interface Invoker {
  // 执行方法，此方法负责完成对象方法的调用和对象属性的读写，三个实现类分别实现不同的业务，读取属性操作，属性赋值操作，方法触发操作
  Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;
  // 获取类型
  Class<?> getType();
}
