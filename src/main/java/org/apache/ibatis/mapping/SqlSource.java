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
package org.apache.ibatis.mapping;

/** 表示从XML文件或注释中读取的映射语句的内容。它创建将从用户接收到的输入参数传递到数据库的SQL。
 * Represents the content of a mapped statement read from an XML file or an annotation. It creates the SQL that will be
 * passed to the database out of the input parameter received from the user.
 * 解析实体的一个接口，对应了MappedStatement中的SQL语句，
 * @author Clinton Begin
 */
public interface SqlSource {
  // 获得一个BoundSql语句，只有这一个功能，根据参数进行得到对应的BoundSql，四个实现类
  BoundSql getBoundSql(Object parameterObject);

}
