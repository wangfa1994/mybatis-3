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

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/** 用来 支持多数据库 规范  两个实现 一个已经废弃
 * Should return an id to identify the type of this database. That id can be used later on to build different queries
 * for each database type This mechanism enables supporting multiple vendors or versions
 *
 * @author Eduardo Macarron
 */
public interface DatabaseIdProvider {

  default void setProperties(Properties p) {
    // NOP  将 MyBatis配置文件中设置在databaseIdProvider节点中的信息写入VendorDatabaseIdProvider对象中。 这些信息实际是数据库的别名信息
  }
  // 获得到当前传入的 DataSource 对象对应的 databaseId
  String getDatabaseId(DataSource dataSource) throws SQLException;
}
