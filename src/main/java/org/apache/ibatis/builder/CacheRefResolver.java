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
package org.apache.ibatis.builder;

import org.apache.ibatis.cache.Cache;

/** 类的解析器类， Mybatis 支持多个nameSpace之间共享缓存 <cache-ref/>标签
 * @author Clinton Begin
 */
public class CacheRefResolver {
  private final MapperBuilderAssistant assistant; // 解析器
  private final String cacheRefNamespace;// 被应用的namespace 被解析的类 的相关属性

  public CacheRefResolver(MapperBuilderAssistant assistant, String cacheRefNamespace) {
    this.assistant = assistant;
    this.cacheRefNamespace = cacheRefNamespace;
  }

  public Cache resolveCacheRef() {
    return assistant.useCacheRef(cacheRefNamespace); // 使用了辅助解析器类的useCacheRef，用来解析缓存共享问题
  }
}
