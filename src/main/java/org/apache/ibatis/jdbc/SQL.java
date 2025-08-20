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
package org.apache.ibatis.jdbc;

/** 继承AbstractSQL 得到自己的 SQL  将AbstractSQL作为抽象方法独立出来，使得我们可以继承AbstractSQL实现其他的子类，保证了 AbstractSQL类更容易被扩展
 * @author Clinton Begin
 */
public class SQL extends AbstractSQL<SQL> {

  @Override
  public SQL getSelf() {
    return this; // 返回SQL自身对象
  }

}
