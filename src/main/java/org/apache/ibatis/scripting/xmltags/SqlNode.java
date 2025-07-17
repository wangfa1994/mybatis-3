/*
 *    Copyright 2009-2022 the original author or authors.
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

/**  sql节点，不同的标签结点有不同的实现  SQL 语句中支持许多种类的节点 if、where、foreach 等，它们都是SqlNode的子类  10个实现类
 * @author Clinton Begin
 */
public interface SqlNode {
  // 完成该节点自身的解析，并将解析结果合并到参数上下文环境context中，返回是否解析成功
  boolean apply(DynamicContext context);
}
