/*
 *    Copyright 2009-2024 the original author or authors.
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.BuilderException;

/**  支持多数据库的sql语句，用来筛选出选择对应数据库的sql语句
 * Vendor DatabaseId provider.
 * <p>
 * It returns database product name as a databaseId. If the user provides a properties it uses it to translate database
 * product name key="Microsoft SQL Server", value="ms" will return "ms". It can return null, if no database product name
 * or a properties was specified and no translation was found.
 *
 * @author Eduardo Macarron
 */
public class VendorDatabaseIdProvider implements DatabaseIdProvider {

  private Properties properties;

  @Override
  public String getDatabaseId(DataSource dataSource) {
    if (dataSource == null) {
      throw new NullPointerException("dataSource cannot be null");
    }
    try {
      return getDatabaseName(dataSource);
    } catch (SQLException e) {
      throw new BuilderException("Error occurred when getting DB product name.", e);
    }
  }

  @Override
  public void setProperties(Properties p) {
    this.properties = p;
  }
  // 首先获取当前数据源的类型，然后将数据源类型映射为我们再databaseIdProvider节点中设置的别名，这样，在需要执行sql语句的时候，就可以根据数据库操作结点中的databaseId设置对sql语句进行筛选
  private String getDatabaseName(DataSource dataSource) throws SQLException {
    String productName = getDatabaseProductName(dataSource); // 获取当前链接的数据库名
    if (properties == null || properties.isEmpty()) {
      return productName;
    } //如果设置了properties值，将获取的数据库名称作为模糊的key，映射为对应的value
    return properties.entrySet().stream().filter(entry -> productName.contains((String) entry.getKey()))
        .map(entry -> (String) entry.getValue()).findFirst().orElse(null);
  }

  private String getDatabaseProductName(DataSource dataSource) throws SQLException {
    try (Connection con = dataSource.getConnection()) {
      return con.getMetaData().getDatabaseProductName();
    }
  }

}
