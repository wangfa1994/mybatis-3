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

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
// 书写映射文件时既能够直接引用实参，又能直接引用实参的属性就是因为这个类
/** 动态上下文 两个功能 ：在进行 SQL节点树的解析时，需要不断保存已经解析完成的 SQL片段
 * @author Clinton Begin  在进行SQL节点树的解析时也需要一些参数和环境信息作为解析的依据
 */
public class DynamicContext {

  public static final String PARAMETER_OBJECT_KEY = "_parameter";
  public static final String DATABASE_ID_KEY = "_databaseId";

  static {
    OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
  }
  // ContextMap类是内部类继承了map
  private final ContextMap bindings; // SQL节点树解析时的上下文环境 ，存储了 数据库 id 参数对象 参数对象的元数据
  private final StringJoiner sqlBuilder = new StringJoiner(" ");  // 存储解析结束的SQL片段
  private int uniqueNumber; // 解析时的唯一编号，防止解析混乱

  public DynamicContext(Configuration configuration, Object parameterObject) {
    if (parameterObject != null && !(parameterObject instanceof Map)) {
      MetaObject metaObject = configuration.newMetaObject(parameterObject); // 获得当前参数的元对象
      boolean existsTypeHandler = configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());// 判断参数对象本身是否有对应的类型处理器
      bindings = new ContextMap(metaObject, existsTypeHandler);// 放入到上下文信息中 【参数对象的元数据:基于参数对象的元数据可以方便地引用参数对象的属性值,因此在编写 SQL语句时可以直接引用参数对象的属性】
    } else {
      bindings = new ContextMap(null, false); // 上下文信息为空
    }
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject); // 把参数对象放入上下文信息中 【参数对象：在编写 SQL 语句时，我们可以直接使用 PARAMETER_OBJECT_KEY变量来引用整个参数对象】
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());//把数据库id放入上下文信息中 【数据库id：我们可以直接使用 DATABASE_ID_KEY变量引用数据库 id的值】
  }

  public Map<String, Object> getBindings() {
    return bindings;
  }

  public void bind(String name, Object value) {
    bindings.put(name, value);
  }

  public void appendSql(String sql) {
    sqlBuilder.add(sql);
  }

  public String getSql() {
    return sqlBuilder.toString().trim();
  }

  public int getUniqueNumber() {
    return uniqueNumber++;
  }
  // 在进行数据查询时，DynamicContext会先从 HashMap中查询，如果查询失败则会从参数对象的属性中查询,基于这一点，我们可以在编写 SQL 语句时直接引用参数对象的属性
  static class ContextMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 2977601501966151582L;
    private final MetaObject parameterMetaObject;
    private final boolean fallbackParameterObject;

    public ContextMap(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
      this.parameterMetaObject = parameterMetaObject;
      this.fallbackParameterObject = fallbackParameterObject;
    }

    @Override
    public Object get(Object key) {
      String strKey = (String) key;
      if (super.containsKey(strKey)) { // 如果map中包含对应的键，直接返回
        return super.get(strKey);
      }

      if (parameterMetaObject == null) {
        return null;
      }
      // 尝试从参数对象的原对象中进行获取
      if (fallbackParameterObject && !parameterMetaObject.hasGetter(strKey)) {
        return parameterMetaObject.getOriginalObject();
      }
      // issue #61 do not modify the context when reading
      return parameterMetaObject.getValue(strKey);
    }
  }

  static class ContextAccessor implements PropertyAccessor {

    @Override
    public Object getProperty(OgnlContext context, Object target, Object name) {
      Map map = (Map) target;

      Object result = map.get(name);
      if (map.containsKey(name) || result != null) {
        return result;
      }

      Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
      if (parameterObject instanceof Map) {
        return ((Map) parameterObject).get(name);
      }

      return null;
    }

    @Override
    public void setProperty(OgnlContext context, Object target, Object name, Object value) {
      Map<Object, Object> map = (Map<Object, Object>) target;
      map.put(name, value);
    }

    @Override
    public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
      return null;
    }

    @Override
    public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
      return null;
    }
  }
}
