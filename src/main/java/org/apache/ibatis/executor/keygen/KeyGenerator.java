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
package org.apache.ibatis.executor.keygen;

import java.sql.Statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

/** 自增主键顶级接口，定义了数据插入前和数据插入后两个规范标准
 * @author Clinton Begin
 */
public interface KeyGenerator {
  // 数据插入前进行的操作 (执行器、映射文件 Statement对象 sql语句实参对象)
  void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);
  // 数据插入后进行的操作 (执行器、映射文件 Statement对象 sql语句实参对象)
  void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
