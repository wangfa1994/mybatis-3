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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import org.apache.ibatis.builder.BuilderException;

/** 缓存OGNL解析表达式  因为有预编译的速度更快
 * Caches OGNL parsed expressions.
 *
 * @author Eduardo Macarron
 *
 * @see <a href='https://github.com/mybatis/old-google-code-issues/issues/342'>Issue 342</a>
 */
public final class OgnlCache {

  private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess(); // Mybatis提供的OGNL的OgnlMemberAccess对象
  private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver(); // Mybatis提供的OGNL的OgnlClassResolver对象
  private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>(); // 缓存表达式

  private OgnlCache() {
    // Prevent Instantiation of Static Class
  }
  // 读取表达式的结果
  public static Object getValue(String expression, Object root) {
    try { // 创建默认的上下文环境，传递了Mybatis提供的 MemberAccess 和 ClassResolver
      OgnlContext context = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
      return Ognl.getValue(parseExpression(expression), context, root);
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
  }

  private static Object parseExpression(String expression) throws OgnlException {
    Object node = expressionCache.get(expression); // 先从缓存中取
    if (node == null) {// 不存在进行解析并缓存
      node = Ognl.parseExpression(expression);
      expressionCache.put(expression, node);
    }
    return node;
  }

}
