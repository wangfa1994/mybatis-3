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
package org.apache.ibatis.scripting.xmltags;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.BuilderException;

/** 表达式求值器  简易封装了OGNL表达式工具类，
 * @author Clinton Begin 基于 OGNL 封装的表达式求值器是 SQL 节点树解析的利器，它能够根据上下文环境对表达式的值做出正确的判断，这是将复杂的数据库操作语句解析为纯粹SQL语句的十分重要的一步
 */
public class ExpressionEvaluator {
  // 对结构为true和false形式的表达式进行求值  针对<if test="name != null"> 节点中的true false判断可以直接进行调用
  public boolean evaluateBoolean(String expression, Object parameterObject) {
    Object value = OgnlCache.getValue(expression, parameterObject); // 得到表达式的值
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) { // 数值型进行一下处理
      return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
    }
    return value != null;
  }

  /** 对结果为迭代形式的表达式进行求值。这样，“＜foreach item="id" collection="array" open="（" separator="，"close="）"＞＃{id} ＜/foreach＞”节点中的迭代判断便可以直接调用该方法完成
   * @deprecated Since 3.5.9, use the {@link #evaluateIterable(String, Object, boolean)}.
   */
  @Deprecated
  public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
    return evaluateIterable(expression, parameterObject, false);
  }

  /**
   * @since 3.5.9
   */
  public Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable) {
    Object value = OgnlCache.getValue(expression, parameterObject); // 获取表达式的结果
    if (value == null) {
      if (nullable) {
        return null;
      }
      throw new BuilderException("The expression '" + expression + "' evaluated to a null value.");
    }
    if (value instanceof Iterable) { // 列表
      return (Iterable<?>) value;
    }
    if (value.getClass().isArray()) { // 数组
      // the array may be primitive, so Arrays.asList() may throw
      // a ClassCastException (issue 209). Do the work manually
      // Curse primitives! :) (JGB)
      int size = Array.getLength(value);
      List<Object> answer = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        Object o = Array.get(value, i);
        answer.add(o);
      }
      return answer;
    }
    if (value instanceof Map) { // 结果为Map
      return ((Map) value).entrySet();
    }
    throw new BuilderException(
        "Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
  }

}
