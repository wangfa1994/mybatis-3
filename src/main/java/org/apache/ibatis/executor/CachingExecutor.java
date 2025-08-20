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
package org.apache.ibatis.executor;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/** 装饰器类
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class CachingExecutor implements Executor {

  private final Executor delegate; // 装饰SimpleExecutor ，通过装饰实际的执行器进行增加二级缓存功能
  private final TransactionalCacheManager tcm = new TransactionalCacheManager(); //事务缓存管理器
  // 事务不仅可以代指封装在一起的多条语句，也可以用来代指一条普通的语句,
  public CachingExecutor(Executor delegate) {
    this.delegate = delegate;
    delegate.setExecutorWrapper(this);
  }

  @Override
  public Transaction getTransaction() {
    return delegate.getTransaction();
  }

  @Override
  public void close(boolean forceRollback) {
    try {
      // issues #499, #524 and #573
      if (forceRollback) {
        tcm.rollback();
      } else {
        tcm.commit();
      }
    } finally {
      delegate.close(forceRollback);
    }
  }

  @Override
  public boolean isClosed() {
    return delegate.isClosed();
  }

  @Override
  public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    flushCacheIfRequired(ms);
    return delegate.update(ms, parameterObject);
  }

  @Override
  public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
    flushCacheIfRequired(ms);
    return delegate.queryCursor(ms, parameter, rowBounds);
  }

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject); //将我们的参数传递进去，进行封装成我们的对象， 这里会根据不同的SqlSource进行处理，$的sqlSource会被处理成完整的sql
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql); //创建我们的缓存key，传进去四个，实际上多运用了一个环境。rowBounds表示分页数据
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler,
      CacheKey key, BoundSql boundSql) throws SQLException {
    Cache cache = ms.getCache(); // 获得到MappedStatement中的二级缓存，如果我们配置了对应的二级缓存配置，这里则会获取对应的值，策略存在的情况下，才会进行二级缓存逻辑
    if (cache != null) {// 如果我们的映射文件中没有配置cache标签的话，则不会存在值
      flushCacheIfRequired(ms); // 执行前，根据配置判断是否需要清除二级缓存，如果需要的话，进行清除
      if (ms.isUseCache() && resultHandler == null) { // 使用缓存且没有输出结果处理器
        ensureNoOutParams(ms, boundSql); // 二级缓存不支持含有输出参数CALLABLE语句，在这里进行判断
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key); // 从缓存中读取结果
        if (list == null) { // 缓存中没有结果
          list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql); // 交给被包装的执行器进行执行
          tcm.putObject(cache, key, list); // issue #578 and #116 将我们的执行器返回的结构进行缓存
        }
        return list;
      }
    } // 缓存不等于null的情况下使用缓存,否则的话通过delegate进行包装的执行器进行执行查询
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public List<BatchResult> flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  @Override
  public void commit(boolean required) throws SQLException {
    delegate.commit(required); //这个会清空一级缓存
    tcm.commit(); // 这里会添加二级缓存
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    try {
      delegate.rollback(required);
    } finally {
      if (required) {
        tcm.rollback();
      }
    }
  }

  private void ensureNoOutParams(MappedStatement ms, BoundSql boundSql) {
    if (ms.getStatementType() == StatementType.CALLABLE) {
      for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
        if (parameterMapping.getMode() != ParameterMode.IN) {
          throw new ExecutorException(
              "Caching stored procedures with OUT params is not supported.  Please configure useCache=false in "
                  + ms.getId() + " statement.");
        }
      }
    }
  }

  @Override
  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
  }

  @Override
  public boolean isCached(MappedStatement ms, CacheKey key) {
    return delegate.isCached(ms, key);
  }

  @Override
  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key,
      Class<?> targetType) {
    delegate.deferLoad(ms, resultObject, property, key, targetType);
  }

  @Override
  public void clearLocalCache() {
    delegate.clearLocalCache();
  }

  private void flushCacheIfRequired(MappedStatement ms) {
    Cache cache = ms.getCache();
    if (cache != null && ms.isFlushCacheRequired()) { // 先判断映射文件中是否配置了cache标签，再进行判断sql片段中是否存在flushCache属性
      tcm.clear(cache);// 如果是需要的话，则通过事务缓存管理器进行清空事务中所有的缓存
    }
  }

  @Override
  public void setExecutorWrapper(Executor executor) {
    throw new UnsupportedOperationException("This method should not be called");
  }

}
