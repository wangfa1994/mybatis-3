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
package org.apache.ibatis.binding;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/** 将方法和SQL语句进行封装的一个对象，只要触发此类的execute方法的调用，机会将对应的方法调用转到对应的sql执行
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 * @author Kazuki Shimizu 包含三个内部类 ParamMap SqlCommand MethodSignature
 */
public class MapperMethod {

  private final SqlCommand command; // 这个是内部类 代指一条SQL，这里会进行计算出我们的MappedStatement
  private final MethodSignature method; // 内部类 ，具体方法签名，描述了一个方法的细节

  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
  }
  // 将方法和sql进行绑定，通过调用方法触发我们的SQL执行 ,sqlSession操作数据库的Java对象，args调用方法时传递的实参
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result; // 根据不同的执行类型进行不同的方法处理 ,根据代理类的方法转换到真正的sqlSession进行数据库相关的处理
    switch (command.getType()) {
      case INSERT: {
        Object param = method.convertArgsToSqlCommandParam(args); // 通过args转换我们的参数，这里使用了参数解析器进行处理，将形参和实参进行绑定
        result = rowCountResult(sqlSession.insert(command.getName(), param)); //执行操作，返回结果
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args); //参数绑定，形参和实参进行一一对应
        result = rowCountResult(sqlSession.update(command.getName(), param)); // 执行操作，返回结果
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.delete(command.getName(), param));
        break;
      }
      case SELECT: // select比较复杂，因为返回值可以是单个，列表，基本类型等
        if (method.returnsVoid() && method.hasResultHandler()) { // 如果返回为空，并且有结果处理器。
          executeWithResultHandler(sqlSession, args); // 使用结果处理器进行执行查询
          result = null;
        } else if (method.returnsMany()) { //如果存在多条结果的话
          result = executeForMany(sqlSession, args);
        } else if (method.returnsMap()) { // map结果查询
          result = executeForMap(sqlSession, args);
        } else if (method.returnsCursor()) { // 游标类型的结果查询
          result = executeForCursor(sqlSession, args);
        } else { // 单条结构的查询
          Object param = method.convertArgsToSqlCommandParam(args);//将我们的实参进行转换赋值到对应的形参上面
          result = sqlSession.selectOne(command.getName(), param);
          if (method.returnsOptional() && (result == null || !method.getReturnType().equals(result.getClass()))) {
            result = Optional.ofNullable(result);
          }
        }
        break;
      case FLUSH: // 清空缓存语句
        result = sqlSession.flushStatements();
        break;
      default: // 未知的操作语句
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) { // 如果查询结果为null，但是返回类型为基本类型，返回变量无法接搜查询结果，抛出异常
      throw new BindingException("Mapper method '" + command.getName()
          + "' attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }

  private Object rowCountResult(int rowCount) {
    final Object result;
    if (method.returnsVoid()) {
      result = null;
    } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
      result = rowCount;
    } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
      result = (long) rowCount;
    } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
      result = rowCount > 0;
    } else {
      throw new BindingException(
          "Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
    }
    return result;
  }

  private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
    MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
    if (!StatementType.CALLABLE.equals(ms.getStatementType())
        && void.class.equals(ms.getResultMaps().get(0).getType())) {
      throw new BindingException(
          "method " + command.getName() + " needs either a @ResultMap annotation, a @ResultType annotation,"
              + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
    }
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
    } else {
      sqlSession.select(command.getName(), param, method.extractResultHandler(args));
    }
  }

  private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
    List<E> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.selectList(command.getName(), param, rowBounds);
    } else {
      result = sqlSession.selectList(command.getName(), param);
    }
    // issue #510 Collections & arrays support
    if (!method.getReturnType().isAssignableFrom(result.getClass())) {
      if (method.getReturnType().isArray()) {
        return convertToArray(result);
      }
      return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
    }
    return result;
  }

  private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
    Cursor<T> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.selectCursor(command.getName(), param, rowBounds);
    } else {
      result = sqlSession.selectCursor(command.getName(), param);
    }
    return result;
  }

  private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
    Object collection = config.getObjectFactory().create(method.getReturnType());
    MetaObject metaObject = config.newMetaObject(collection);
    metaObject.addAll(list);
    return collection;
  }

  @SuppressWarnings("unchecked")
  private <E> Object convertToArray(List<E> list) {
    Class<?> arrayComponentType = method.getReturnType().getComponentType();
    Object array = Array.newInstance(arrayComponentType, list.size());
    if (!arrayComponentType.isPrimitive()) {
      return list.toArray((E[]) array);
    }
    for (int i = 0; i < list.size(); i++) {
      Array.set(array, i, list.get(i));
    }
    return array;
  }

  private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
    Map<K, V> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.selectMap(command.getName(), param, method.getMapKey(), rowBounds);
    } else {
      result = sqlSession.selectMap(command.getName(), param, method.getMapKey());
    }
    return result;
  }
  /*内部类用来存储参数，是HashMap的子类，比hashMap获取数据更严格，因为会先判断是否有这个值*/
  public static class ParamMap<V> extends HashMap<String, V> {

    private static final long serialVersionUID = -2212268410512043556L;

    @Override
    public V get(Object key) {
      if (!super.containsKey(key)) {
        throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
      }
      return super.get(key);
    }

  }

  public static class SqlCommand { // 代指一条SQL语句

    private final String name; // SQL语句的名称
    private final SqlCommandType type; // sql语句的种类，一共六种：增，删，改，查，清缓存，未知

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) { //构造器方法传递了一些信息
      final String methodName = method.getName();
      final Class<?> declaringClass = method.getDeclaringClass();
      MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass, configuration); //得到我们的 MappedStatement
      if (ms == null) {
        if (method.getAnnotation(Flush.class) == null) {
          throw new BindingException(
              "Invalid bound statement (not found): " + mapperInterface.getName() + "." + methodName);
        }
        name = null;
        type = SqlCommandType.FLUSH;
      } else { // 解析出来我们的name和type
        name = ms.getId();
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) {
          throw new BindingException("Unknown execution method for: " + name);
        }
      }
    }

    public String getName() {
      return name;
    }

    public SqlCommandType getType() {
      return type;
    }
    //  mapperInterface 映射接口 methodName 具体方法名  declaringClass 操作方法所在的类 configuration配置信息
    private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName, Class<?> declaringClass,
        Configuration configuration) {
      String statementId = mapperInterface.getName() + "." + methodName; // 映射接口的名字+方法名字，组成唯一的数据库操作语句编号
      if (configuration.hasStatement(statementId)) { //从configuration中找到对应的语句， configuration中保存了解析后的所有操作语句
        return configuration.getMappedStatement(statementId);
      }
      if (mapperInterface.equals(declaringClass)) { // 这里判断的是参数一 和 参数三
        return null; // 递归到了终点，返回null
      }
      for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
          MappedStatement ms = resolveMappedStatement(superInterface, methodName, declaringClass, configuration);// 向上循环调用，进行所有方法的判断
          if (ms != null) {
            return ms;
          }
        }
      }
      return null;
    }
  }
  // 当一个类中的属性特别多的时候，是不是可以进行对应的 属性归类，提取内部类
  public static class MethodSignature { // 指代一个具体方法的签名

    private final boolean returnsMany; //判断该方法返回类型是否为集合类型
    private final boolean returnsMap; // 判断该方法返回类型是否为Map类型
    private final boolean returnsVoid; // 判断该方法返回类型是否为为空
    private final boolean returnsCursor; // 判断该方法返回类型是否为Cursor类型
    private final boolean returnsOptional; //判断该方法返回类型是否为Optional类型
    private final Class<?> returnType; // 方法的返回类型
    private final String mapKey; // 如果方法返回的是Map,这里记录所有map的key
    private final Integer resultHandlerIndex; // resultHandler参数的位置
    private final Integer rowBoundsIndex; // rowBounds 参数的位置
    private final ParamNameResolver paramNameResolver; // 参数名称解析器，这里是用来解析对应的参数信息

    public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      if (resolvedReturnType instanceof Class<?>) {
        this.returnType = (Class<?>) resolvedReturnType;
      } else if (resolvedReturnType instanceof ParameterizedType) {
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
      } else {
        this.returnType = method.getReturnType();
      }
      this.returnsVoid = void.class.equals(this.returnType);
      this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
      this.returnsCursor = Cursor.class.equals(this.returnType);
      this.returnsOptional = Optional.class.equals(this.returnType);
      this.mapKey = getMapKey(method);
      this.returnsMap = this.mapKey != null;
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
      this.paramNameResolver = new ParamNameResolver(configuration, method); //创建了针对方法解析的参数解析器， 这里会解析出来我们对应的参数和索引位置
    }

    public Object convertArgsToSqlCommandParam(Object[] args) { // 再进行方法调用的时候，会进行此方法的调用，用来进行映射我们参数的相关真实数据
      return paramNameResolver.getNamedParams(args); // 构造器的时候进行了创建此对象，然后再使用的时候直接进行使用，传递的是实参，需要和我们的形参进行绑定
    }

    public boolean hasRowBounds() {
      return rowBoundsIndex != null;
    }

    public RowBounds extractRowBounds(Object[] args) {
      return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
    }

    public boolean hasResultHandler() {
      return resultHandlerIndex != null;
    }

    public ResultHandler extractResultHandler(Object[] args) {
      return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
    }

    public Class<?> getReturnType() {
      return returnType;
    }

    public boolean returnsMany() {
      return returnsMany;
    }

    public boolean returnsMap() {
      return returnsMap;
    }

    public boolean returnsVoid() {
      return returnsVoid;
    }

    public boolean returnsCursor() {
      return returnsCursor;
    }

    /**
     * return whether return type is {@code java.util.Optional}.
     *
     * @return return {@code true}, if return type is {@code java.util.Optional}
     *
     * @since 3.5.0
     */
    public boolean returnsOptional() {
      return returnsOptional;
    }

    private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
      Integer index = null;
      final Class<?>[] argTypes = method.getParameterTypes();
      for (int i = 0; i < argTypes.length; i++) {
        if (paramType.isAssignableFrom(argTypes[i])) {
          if (index != null) {
            throw new BindingException(
                method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
          }
          index = i;
        }
      }
      return index;
    }

    public String getMapKey() {
      return mapKey;
    }

    private String getMapKey(Method method) {
      String mapKey = null;
      if (Map.class.isAssignableFrom(method.getReturnType())) {
        final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
        if (mapKeyAnnotation != null) {
          mapKey = mapKeyAnnotation.value();
        }
      }
      return mapKey;
    }
  }

}
