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
package org.apache.ibatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/** 第二级缓存事务缓冲区。
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session. Entries are sent to
 * the cache when commit is called or discarded if the Session is rolled back. Blocking cache support has been added.
 * Therefore any get() that returns a cache miss will be followed by a put() so any lock associated with the key can be
 * released.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class TransactionalCache implements Cache {

  private static final Log log = LogFactory.getLog(TransactionalCache.class);

  private final Cache delegate; // 被装饰的缓存对象
  private boolean clearOnCommit; //事务提交之后，是否直接清理掉缓存，这个清理的是整个缓存
  private final Map<Object, Object> entriesToAddOnCommit; // 事务过程中产生的数据暂时保存，在事务提交的时候进行一并提交给缓存，在事务回滚的时候就直接销毁了，为了防止脏读，所以缓存也需要在提价的时候写入
  private final Set<Object> entriesMissedInCache; // 缓存查询未命中的数据
  //事务缓存中使用的缓存可能被BlockingCache装饰过，这样的话，如果缓存查询得到的结果为 null，会导致对该数据上锁，从而阻塞后续对该数据的查询,而事务提交或者回滚后，应该对缓存中的这些数据全部解锁。entriesMissedInCache就保存了这些数据的键，在事务结束时低这些数据进行解锁
  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public Object getObject(Object key) { // 从缓存中得到一条记录
    // issue #116
    Object object = delegate.getObject(key); // 从缓存中读取对应的数据
    if (object == null) { // 没有没有读到，进行缓存未命中的缓存key
      entriesMissedInCache.add(key);
    }
    // issue #146
    if (clearOnCommit) { // 如果设置了提交立即清除，直接返回null
      return null;
    }
    return object;
  }

  @Override
  public void putObject(Object key, Object object) {
    entriesToAddOnCommit.put(key, object); // 写缓存并没有直接写入到我们对应的包装缓存中，而是先缓存到了事务列表中
  } // 在事务进行回滚或者提交的时候，会根据设置进行写入真正的缓存或者清除

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  @Override
  public void clear() {
    clearOnCommit = true;
    entriesToAddOnCommit.clear();
  }

  public void commit() {
    if (clearOnCommit) { // 如果设置了提交事务之后清理缓存，直接清理掉所有的缓存 ，不可能进行缓存更新的
      delegate.clear();
    }
    flushPendingEntries(); // 将当前事务产生的缓存保存下，未命中的移除下
    reset(); // 清理环境
  }

  public void rollback() { // 回滚
    unlockMissedEntries(); // 清理一下未命中的数据
    reset(); // 还原环境
  }

  private void reset() {
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  private void flushPendingEntries() {
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    for (Object entry : entriesMissedInCache) {
      if (!entriesToAddOnCommit.containsKey(entry)) {
        delegate.putObject(entry, null);
      }
    }
  }

  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
            + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
      }
    }
  }

}
