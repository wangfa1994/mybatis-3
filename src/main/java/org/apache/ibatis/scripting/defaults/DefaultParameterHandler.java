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
package org.apache.ibatis.scripting.defaults;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/** 参数赋值实现
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class DefaultParameterHandler implements ParameterHandler {

  private final TypeHandlerRegistry typeHandlerRegistry; // 类型处理器注册表

  private final MappedStatement mappedStatement; // mappedStatement对象，包括完成的增删改查节点信息
  private final Object parameterObject; // 参数对象
  private final BoundSql boundSql; // boundSql对象 包含sql语句 参数 实参信息
  private final Configuration configuration; // 配置信息

  public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    this.mappedStatement = mappedStatement;
    this.configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
    this.parameterObject = parameterObject;
    this.boundSql = boundSql;
  }

  @Override
  public Object getParameterObject() {
    return parameterObject;
  }

  @Override //完成sql语句对应的变量赋值
  public void setParameters(PreparedStatement ps) { // MyBatis 中支持进行参数设置的语句类型是 PreparedStatement 接口及其子接口（CallableStatement 是 PreparedStatement 的子接口）
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings(); // 取出参数列表
    if (parameterMappings != null) {
      MetaObject metaObject = null;
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
        if (parameterMapping.getMode() != ParameterMode.OUT) { // OUT是CallableStatement的输出参数，已经单独注册，所以忽略，进行IN和INOUT的处理
          Object value;
          String propertyName = parameterMapping.getProperty();// 取出属性名称
          if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
            value = boundSql.getAdditionalParameter(propertyName); // 从附加参数中读取属性值
          } else if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {// 参数对象如果是基本类型，则参数对象即为参数值
            value = parameterObject;
          } else {// 参数对象是复杂的类型，取出参数对象的属性值
            if (metaObject == null) {
              metaObject = configuration.newMetaObject(parameterObject);
            }
            value = metaObject.getValue(propertyName);// 属性值
          }
          TypeHandler typeHandler = parameterMapping.getTypeHandler(); // 获取到此参数的类型转换器，然后进行设置值
          JdbcType jdbcType = parameterMapping.getJdbcType();
          if (value == null && jdbcType == null) {
            jdbcType = configuration.getJdbcTypeForNull();
          }
          try { // 此方法最终根据参数类型调用java.sql.PreparedStatement类中的参数赋值方法，对sql语句中的参数赋值
            typeHandler.setParameter(ps, i + 1, value, jdbcType); // 注意把我们的PreparedStatement进行传递，然后进行设置值
          } catch (TypeException | SQLException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          }
        }
      }
    }
  }

}
