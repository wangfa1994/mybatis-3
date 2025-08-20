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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.builder.InitializingObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.decorators.LoggingCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.ScheduledCache;
import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.SynchronizedCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/** 缓存建造者类，负责完成缓存对象的创建  装饰器模式的强大与灵活，进行了多层包装
 * @author Clinton Begin
 */
public class CacheBuilder {
  private final String id;
  private Class<? extends Cache> implementation; // 缓存实现类
  private final List<Class<? extends Cache>> decorators; // 缓存装饰器列表
  private Integer size; // 缓存大小
  private Long clearInterval; // 是否需要 定时清理的缓存装饰器参数
  private boolean readWrite; // 是否需要SerializedCache装饰器
  private Properties properties; // 缓存的属性设置
  private boolean blocking; // 是否需要阻塞装饰器

  public CacheBuilder(String id) {
    this.id = id;
    this.decorators = new ArrayList<>();
  }

  public CacheBuilder implementation(Class<? extends Cache> implementation) {
    this.implementation = implementation;
    return this;
  }

  public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
    if (decorator != null) {
      this.decorators.add(decorator);
    }
    return this;
  }

  public CacheBuilder size(Integer size) {
    this.size = size;
    return this;
  }

  public CacheBuilder clearInterval(Long clearInterval) {
    this.clearInterval = clearInterval;
    return this;
  }

  public CacheBuilder readWrite(boolean readWrite) {
    this.readWrite = readWrite;
    return this;
  }

  public CacheBuilder blocking(boolean blocking) {
    this.blocking = blocking;
    return this;
  }

  public CacheBuilder properties(Properties properties) {
    this.properties = properties;
    return this;
  }
  // 组建我们的缓存对象
  public Cache build() {
    setDefaultImplementations(); // 设置缓存的默认实现，默认装饰器，仅设置没有进行装配
    Cache cache = newBaseCacheInstance(implementation, id); // 创建默认的缓存实现类，传递的id就是缓存id
    setCacheProperties(cache); // 设置缓存属性
    // issue #352, do not apply decorators to custom caches
    if (PerpetualCache.class.equals(cache.getClass())) { // 默认缓存实现类的逻辑
      for (Class<? extends Cache> decorator : decorators) {//如果我们存在缓存装饰器，进行装饰
        cache = newCacheDecoratorInstance(decorator, cache); // 生成装饰器实例
        setCacheProperties(cache); // 为装饰器的缓存继续设置缓存属性
      }
      cache = setStandardDecorators(cache); // 最后增加系统标准的装饰器
    } else if (!LoggingCache.class.isAssignableFrom(cache.getClass())) { // 如果不是默认的实现的缓存逻辑，并且还不是LoggingCache的话，就用LoggingCache进行包装一下得了，其他的就不需要了
      cache = new LoggingCache(cache);
    }
    return cache;
  }

  private void setDefaultImplementations() { // 设置我们默认的缓存实现，这里只有缓存实现，没有进行装配
    if (implementation == null) { // 没有设置实现的时候，默认的就是我们的 PerpetualCache
      implementation = PerpetualCache.class;
      if (decorators.isEmpty()) {
        decorators.add(LruCache.class); // 默认的清理器是 LruCache，只是进行放入，并没有进行真正的实例
      }
    }
  }

  private Cache setStandardDecorators(Cache cache) { // 设置标准的装饰器
    try {
      MetaObject metaCache = SystemMetaObject.forObject(cache);
      if (size != null && metaCache.hasSetter("size")) {
        metaCache.setValue("size", size); // 设置缓存大小
      }
      if (clearInterval != null) { // 如果定义了时间间隔，进行使用定时清理装饰器装饰缓存
        cache = new ScheduledCache(cache);
        ((ScheduledCache) cache).setClearInterval(clearInterval);
      }
      if (readWrite) { // 允许读写，使用序列化装饰器装饰
        cache = new SerializedCache(cache);
      }
      cache = new LoggingCache(cache); // 默认的一定存在 日志装饰器装饰缓存LoggingCache
      cache = new SynchronizedCache(cache); // 默认的一定存在同步装饰器装饰缓存 SynchronizedCache
      if (blocking) { // 如果启动了阻塞功能，则使用阻塞装饰器装饰缓存
        cache = new BlockingCache(cache);
      }
      return cache; // 返回被层层装饰的缓存
    } catch (Exception e) {
      throw new CacheException("Error building standard cache decorators.  Cause: " + e, e);
    }
  }

  private void setCacheProperties(Cache cache) {
    if (properties != null) {
      MetaObject metaCache = SystemMetaObject.forObject(cache); // 得到我们的缓存元对象信息
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String name = (String) entry.getKey();
        String value = (String) entry.getValue();
        if (metaCache.hasSetter(name)) { // 如果存在对应的属性名称进行设置
          Class<?> type = metaCache.getSetterType(name);
          if (String.class == type) {
            metaCache.setValue(name, value);
          } else if (int.class == type || Integer.class == type) {
            metaCache.setValue(name, Integer.valueOf(value));
          } else if (long.class == type || Long.class == type) {
            metaCache.setValue(name, Long.valueOf(value));
          } else if (short.class == type || Short.class == type) {
            metaCache.setValue(name, Short.valueOf(value));
          } else if (byte.class == type || Byte.class == type) {
            metaCache.setValue(name, Byte.valueOf(value));
          } else if (float.class == type || Float.class == type) {
            metaCache.setValue(name, Float.valueOf(value));
          } else if (boolean.class == type || Boolean.class == type) {
            metaCache.setValue(name, Boolean.valueOf(value));
          } else if (double.class == type || Double.class == type) {
            metaCache.setValue(name, Double.valueOf(value));
          } else {
            throw new CacheException("Unsupported property type for cache: '" + name + "' of type " + type);
          }
        }
      }
    }
    if (InitializingObject.class.isAssignableFrom(cache.getClass())) {
      try {
        ((InitializingObject) cache).initialize();
      } catch (Exception e) {
        throw new CacheException(
            "Failed cache initialization for '" + cache.getId() + "' on '" + cache.getClass().getName() + "'", e);
      }
    }
  }

  private Cache newBaseCacheInstance(Class<? extends Cache> cacheClass, String id) {
    Constructor<? extends Cache> cacheConstructor = getBaseCacheConstructor(cacheClass);
    try {
      return cacheConstructor.newInstance(id);
    } catch (Exception e) {
      throw new CacheException("Could not instantiate cache implementation (" + cacheClass + "). Cause: " + e, e);
    }
  }

  private Constructor<? extends Cache> getBaseCacheConstructor(Class<? extends Cache> cacheClass) {
    try {
      return cacheClass.getConstructor(String.class);
    } catch (Exception e) {
      throw new CacheException("Invalid base cache implementation (" + cacheClass + ").  "
          + "Base cache implementations must have a constructor that takes a String id as a parameter.  Cause: " + e,
          e);
    }
  }

  private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache base) {
    Constructor<? extends Cache> cacheConstructor = getCacheDecoratorConstructor(cacheClass);
    try {
      return cacheConstructor.newInstance(base);
    } catch (Exception e) {
      throw new CacheException("Could not instantiate cache decorator (" + cacheClass + "). Cause: " + e, e);
    }
  }

  private Constructor<? extends Cache> getCacheDecoratorConstructor(Class<? extends Cache> cacheClass) {
    try {
      return cacheClass.getConstructor(Cache.class);
    } catch (Exception e) {
      throw new CacheException("Invalid cache decorator (" + cacheClass + ").  "
          + "Cache decorators must have a constructor that takes a Cache instance as a parameter.  Cause: " + e, e);
    }
  }
}
