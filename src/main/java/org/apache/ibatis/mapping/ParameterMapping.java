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

import java.sql.ResultSet;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**   parameterMap标签中的子标签parameter的映射实体类
 * @author Clinton Begin
 */
public class ParameterMapping {

  private Configuration configuration;

  private String property;
  private ParameterMode mode;
  private Class<?> javaType = Object.class;
  private JdbcType jdbcType;
  private Integer numericScale;
  private TypeHandler<?> typeHandler; // 存放我们的参数值
  private String resultMapId;
  private String jdbcTypeName;
  private String expression;

  private ParameterMapping() {
  }

  public static class Builder {
    private final ParameterMapping parameterMapping = new ParameterMapping();

    public Builder(Configuration configuration, String property, TypeHandler<?> typeHandler) {
      parameterMapping.configuration = configuration;
      parameterMapping.property = property;
      parameterMapping.typeHandler = typeHandler;
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder(Configuration configuration, String property, Class<?> javaType) {
      parameterMapping.configuration = configuration;
      parameterMapping.property = property;
      parameterMapping.javaType = javaType;
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder mode(ParameterMode mode) {
      parameterMapping.mode = mode;
      return this;
    }

    public Builder javaType(Class<?> javaType) {
      parameterMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      parameterMapping.jdbcType = jdbcType;
      return this;
    }

    public Builder numericScale(Integer numericScale) {
      parameterMapping.numericScale = numericScale;
      return this;
    }

    public Builder resultMapId(String resultMapId) {
      parameterMapping.resultMapId = resultMapId;
      return this;
    }

    public Builder typeHandler(TypeHandler<?> typeHandler) {
      parameterMapping.typeHandler = typeHandler;
      return this;
    }

    public Builder jdbcTypeName(String jdbcTypeName) {
      parameterMapping.jdbcTypeName = jdbcTypeName;
      return this;
    }

    public Builder expression(String expression) {
      parameterMapping.expression = expression;
      return this;
    }

    public ParameterMapping build() {
      resolveTypeHandler();
      validate();
      return parameterMapping;
    }

    private void validate() {
      if (ResultSet.class.equals(parameterMapping.javaType)) {
        if (parameterMapping.resultMapId == null) {
          throw new IllegalStateException("Missing resultmap in property '" + parameterMapping.property + "'.  "
              + "Parameters of type java.sql.ResultSet require a resultmap.");
        }
      } else if (parameterMapping.typeHandler == null) {
        throw new IllegalStateException("Type handler was null on parameter mapping for property '"
            + parameterMapping.property + "'. It was either not specified and/or could not be found for the javaType ("
            + parameterMapping.javaType.getName() + ") : jdbcType (" + parameterMapping.jdbcType + ") combination.");
      }
    }

    private void resolveTypeHandler() {
      if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
        Configuration configuration = parameterMapping.configuration;
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        parameterMapping.typeHandler = typeHandlerRegistry.getTypeHandler(parameterMapping.javaType,
            parameterMapping.jdbcType);
      }
    }

  }

  public String getProperty() {
    return property;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the mode
   */
  public ParameterMode getMode() {
    return mode;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the java type
   */
  public Class<?> getJavaType() {
    return javaType;
  }

  /**
   * Used in the UnknownTypeHandler in case there is no handler for the property type.
   *
   * @return the jdbc type
   */
  public JdbcType getJdbcType() {
    return jdbcType;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the numeric scale
   */
  public Integer getNumericScale() {
    return numericScale;
  }

  /**
   * Used when setting parameters to the PreparedStatement.
   *
   * @return the type handler
   */
  public TypeHandler<?> getTypeHandler() {
    return typeHandler;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the result map id
   */
  public String getResultMapId() {
    return resultMapId;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the jdbc type name
   */
  public String getJdbcTypeName() {
    return jdbcTypeName;
  }

  /**
   * Expression 'Not used'.
   *
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ParameterMapping{");
    // sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
    sb.append("property='").append(property).append('\'');
    sb.append(", mode=").append(mode);
    sb.append(", javaType=").append(javaType);
    sb.append(", jdbcType=").append(jdbcType);
    sb.append(", numericScale=").append(numericScale);
    // sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
    sb.append(", resultMapId='").append(resultMapId).append('\'');
    sb.append(", jdbcTypeName='").append(jdbcTypeName).append('\'');
    sb.append(", expression='").append(expression).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
