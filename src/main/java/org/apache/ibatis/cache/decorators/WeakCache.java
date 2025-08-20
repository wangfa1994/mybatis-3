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
package org.apache.ibatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.cache.Cache;

/** 清理缓存器：针对缓存增加了清理策略,将缓存数据包装成弱引用的数据,从而使得JVM可以清理掉缓存数据
 * Weak Reference cache decorator.
 * <p>
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 *
 * @author Clinton Begin
 */
public class WeakCache implements Cache {
  private final Deque<Object> hardLinksToAvoidGarbageCollection; // 强引用的对象列表
  private final ReferenceQueue<Object> queueOfGarbageCollectedEntries; // 弱引用的对象列表
  private final Cache delegate; //被装饰的对象
  private int numberOfHardLinks; // 强引用的数量限制
  private final ReentrantLock lock = new ReentrantLock();

  public WeakCache(Cache delegate) {
    this.delegate = delegate;
    this.numberOfHardLinks = 256;
    this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
    this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    removeGarbageCollectedItems();
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.numberOfHardLinks = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    removeGarbageCollectedItems();
    delegate.putObject(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries)); // 放入的时候，包装成了一个内部类对象
  }

  @Override
  public Object getObject(Object key) {
    Object result = null;
    @SuppressWarnings("unchecked") // assumed delegate cache is totally managed by this cache
    WeakReference<Object> weakReference = (WeakReference<Object>) delegate.getObject(key);
    if (weakReference != null) { // 得到弱引用
      result = weakReference.get();
      if (result == null) { // 弱引用中的值为空，直接移除掉
        delegate.removeObject(key);
      } else {
        lock.lock(); // 进行加锁，将弱引用的对象加入到强引用列表中
        try {
          hardLinksToAvoidGarbageCollection.addFirst(result);
          if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {// 如果强引用的对象数目超出了限制
            hardLinksToAvoidGarbageCollection.removeLast();// 从强引用的列表中删除该数据
          }
        } finally {
          lock.unlock();
        }
      }
    }
    return result;
  }

  @Override
  public Object removeObject(Object key) {
    removeGarbageCollectedItems();
    @SuppressWarnings("unchecked")
    WeakReference<Object> weakReference = (WeakReference<Object>) delegate.removeObject(key);
    return weakReference == null ? null : weakReference.get();
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      hardLinksToAvoidGarbageCollection.clear();
    } finally {
      lock.unlock();
    }
    removeGarbageCollectedItems();
    delegate.clear();
  }

  private void removeGarbageCollectedItems() { // 清理垃圾回收队列中的元素
    WeakEntry sv;
    while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {//处理垃圾回收队列中的数据，从队列中取出来，进行移除
      delegate.removeObject(sv.key);
    }
  }

  private static class WeakEntry extends WeakReference<Object> {
    private final Object key;

    private WeakEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
      super(value, garbageCollectionQueue); // 传递进去的garbageCollectionQueue队列，当进行回收的时候，会放入到队列中
      this.key = key;
    }
  }

}
