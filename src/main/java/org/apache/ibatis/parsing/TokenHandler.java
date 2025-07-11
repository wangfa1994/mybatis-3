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
package org.apache.ibatis.parsing;

/**  解析式的顶级接口，根据表达式，进行解析出来真正的值,不同的表达式，有不同的开闭符号，有不同的解析式实现进行处理
 * @author Clinton Begin
 */
public interface TokenHandler {
  /** 根据传入的内容进行匹配对应的值*/
  String handleToken(String content);
}
