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
package org.apache.ibatis.reflection;
// 创建Reflector对象的工厂， 传递一个Class ,获得此Class对象的Reflector对象，
public interface ReflectorFactory {
  // 是否允许缓存
  boolean isClassCacheEnabled();

  void setClassCacheEnabled(boolean classCacheEnabled);
  // 给定一个class 进行返回这个class的封装对象Reflector对象
  Reflector findForClass(Class<?> type);
}
