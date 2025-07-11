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
package org.apache.ibatis.builder;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/** 静态语句，可能含有?的语句，可以直接交给数据库进行执行的  【DynamicSqlSource语句和RawSqlSource都会被处理成StaticSqlSource，然后通过getBoundSql得到BoundSql】
 * @author Clinton Begin
 */
public class StaticSqlSource implements SqlSource {

  private final String sql; // 移除占位符的sql语句，sql可以直接执行的预编译语句 不存在#{}和${}这种符号的语句，只有?占位符的sql
  private final List<ParameterMapping> parameterMappings; // 参数列表
  private final Configuration configuration; // 配置信息

  public StaticSqlSource(Configuration configuration, String sql) {
    this(configuration, sql, null);
  }

  public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.configuration = configuration;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) { // 很重要的功能，给出我们的BoundSql对象
    return new BoundSql(configuration, sql, parameterMappings, parameterObject);
  }

}
