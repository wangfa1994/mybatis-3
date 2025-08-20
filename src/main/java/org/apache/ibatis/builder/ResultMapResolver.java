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

import java.util.List;

import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

/**  ResultMap解析器类， 解析ResultMap标签，支持继承，用来解析继承相关属性
 * @author Eduardo Macarron
 */
public class ResultMapResolver {
  private final MapperBuilderAssistant assistant; //解析器
  private final String id; // ResultMap的id
  private final Class<?> type; // ResultMap的type属性
  private final String extend;
  private final Discriminator discriminator;  // ResultMap的鉴别器节点 discriminator节点
  private final List<ResultMapping> resultMappings; // ResultMap标签的属性映射列表
  private final Boolean autoMapping;  //是否开启了自动映射

  public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, String extend,
      Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) {
    this.assistant = assistant;
    this.id = id;
    this.type = type;
    this.extend = extend;
    this.discriminator = discriminator;
    this.resultMappings = resultMappings;
    this.autoMapping = autoMapping;
  }

  public ResultMap resolve() { // 通过我们的  MapperBuilderAssistant 助手进行了resultMap的处理
    return assistant.addResultMap(this.id, this.type, this.extend, this.discriminator, this.resultMappings,
        this.autoMapping);
  }

}
