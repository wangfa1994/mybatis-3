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
package org.apache.ibatis.plugin;

import java.util.Properties;

/** 拦截器接口规范
 * @author Clinton Begin
 */
public interface Interceptor {
  //拦截器类必须实现该方法。拦截器拦截到目标方法时，会将操作转接到该 intercept 方法上，其中的参数 Invocation 为拦截到的目标方法
  Object intercept(Invocation invocation) throws Throwable;
  // 输出一个对象来替换输入参数传入的目标对象
  default Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }
  // 为拦截器设置属性，在进行解析的时候，通过反射得到对象之后就直接进行回调赋值了
  default void setProperties(Properties properties) {
    // NOP
  }

}
