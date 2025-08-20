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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.cache.Cache;

/** 清理缓存器：针对缓存增加了清理策略， 近期最少使用算法,缓存数据数量达到设置的上限时将近期未使用的数据删除
 * Lru (least recently used) cache decorator.
 * 为了删除最久未使用的数据两个步骤： 1.在每次进行缓存查询操作时更新 keyMap中键的排序，将当前被查询的键排到最前面 2.在每次进行缓存写入操作时向 keyMap写入新的键，并且在当前缓存中数据量超过设置的数据量时删除最久未访问的数据
 * @author Clinton Begin
 */
public class LruCache implements Cache {

  private final Cache delegate;
  private Map<Object, Object> keyMap; // 使用map进行保存缓存数据的键，重新了方法
  private Object eldestKey; // 最近最少使用的数据的键

  public LruCache(Cache delegate) {
    this.delegate = delegate;
    setSize(1024);
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(final int size) { // 匿名内部类的写法，创建LinkedHashMap的子类实例，并重写了removeEldestEntry方法，常见于需要对集合类进行定制行为（如实现 LRU 缓存）的场景
    keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
      private static final long serialVersionUID = 4267176411845948333L;

      @Override // LinkedHashMap的方法，该方法会在每次向LinkedHashMap中放入数据（put方法和 putAll方法）后被自动触发,其输入参数为最久未访问的元素
      protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
        boolean tooBig = size() > size; // 如果keyMap的SIZE太大了，大于初始化容量了
        if (tooBig) { // 超出缓存空间的情况下将最久未访问的键放入eldestKey属性中
          eldestKey = eldest.getKey();
        }
        return tooBig;
      }
    };
  }

  @Override
  public void putObject(Object key, Object value) {
    delegate.putObject(key, value); // 正常的进行设置缓存
    cycleKeyList(key); // 装饰逻辑
  }

  @Override
  public Object getObject(Object key) {
    keyMap.get(key); // touch  触及一下当前被访问的缓存，表名当前key被访问了，防止LinkedHashMap进行误判?
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    keyMap.remove(key);
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyMap.clear();
  }

  private void cycleKeyList(Object key) {
    keyMap.put(key, key); // 将key进行放入到keyMap中，并且LinkHashMap会自动回调removeEldestEntry方法，而在这个方法中进行了是否需要给eldestKey进行赋值
    if (eldestKey != null) { // 如果存在最早未使用的变量，进行移除缓存
      delegate.removeObject(eldestKey);
      eldestKey = null;
    }
  }

}
