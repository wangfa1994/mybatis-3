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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;

/** 用来产生对应的ObjectWrapper对象的工厂  【】
 * @author Clinton Begin
 */
public interface ObjectWrapperFactory {

  // 判断对象是否是被包装的，如果是可以直接调用下面的方法进行得到对应的对象包装类
  boolean hasWrapperFor(Object object);
  //从MetaObject 和 Object 中得到对应的对象包装 MetaObject是控制元信息的
  ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
