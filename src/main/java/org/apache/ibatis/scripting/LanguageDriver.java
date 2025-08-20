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
package org.apache.ibatis.scripting;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
// 语言驱动类的接口,  主要用于支持不同的语言来编写sql语句(xml,注解等)，提供统一的接口来解析这些sql语句 不同的语言存在不同的SqlSource,通过语言驱动类接口进行加载sqlSource 存在四个实现类，但是能用的只有一个XmL
public interface LanguageDriver {

  /** 创建一个参数处理器，参数处理器能将实际参数传递给 Jdbc statement
   * Creates a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement.
   *
   * @author Frank D. Martinez [mnesarco]
   *
   * @param mappedStatement 正在执行的映射语句   （完整的数据库操作结点）
   *          The mapped statement that is being executed
   * @param parameterObject 输入参数对象 实例对象
   *          The input parameter object (can be null)
   * @param boundSql  执行动态语言后的结果SQL。  (数据库操作语句转化的BoundSql对象)
   *          The resulting SQL once the dynamic language has been executed.
   *
   * @return the parameter handler
   *
   * @see DefaultParameterHandler
   */
  ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

  /** 创建SqlSource对象(基于映射文件的方式) 该方法在Mybatis启动阶段读取映射接口或映射文件时被调用
   * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file. It is called during startup,
   * when the mapped statement is read from a class or an xml file.
   *
   * @param configuration  配置信息
   *          The MyBatis configuration
   * @param script  映射文件中的数据库操作节点
   *          XNode parsed from a XML file
   * @param parameterType  参数类型
   *          input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be
   *          null.
   *
   * @return the sql source
   */
  SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

  /** 创建sqlSource对象(基于注解的方式) 该方法在mybaits启动阶段读取映射接口或映射文件时被调用
   * Creates an {@link SqlSource} that will hold the statement read from an annotation. It is called during startup,
   * when the mapped statement is read from a class or an xml file.
   *
   * @param configuration   配置信息
   *          The MyBatis configuration
   * @param script  注解中的sql字符串
   *          The content of the annotation
   * @param parameterType 参数类型
   *          input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be
   *          null.
   * 返回的sqlSource对象，具体来说是DynamicSqlSource 和RawSqlSource中的一种
   * @return the sql source
   */
  SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

}
