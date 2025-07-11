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

/** 通用的表达式解析，用来解析对应的表达式中的不同的占位符,可以定义我们自己的占位符和结束符，并且需要扩展TokenHandler，PropertyParser 和 XPathParser是封装的针对性的工具类
 * @author Clinton Begin
 */
public class GenericTokenParser {

  private final String openToken; // 占位符的开头
  private final String closeToken; // 占位符的结束
  private final TokenHandler handler; // 解析出来的表达式，需要被handler处理得到真正的结果

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) { //text:select * from user where id = #{id}
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    int start = text.indexOf(openToken); //查找以#{开头的位置 直接定位到为止，前面的不动直接拼接
    if (start == -1) {
      return text; // 如果等于-1表示没有占位符，直接返回原始信息即可
    }
    char[] src = text.toCharArray(); // 表达式转换，直接变char
    int offset = 0;
    final StringBuilder builder = new StringBuilder(); //解析之后的表达式存放
    StringBuilder expression = null; // 解析出来表达式
    do {
      if (start > 0 && src[start - 1] == '\\') { // 如果前面的#{ 前面的一个字符是\表示是转义字符，需要保留
        // this open token is escaped. remove the backslash and continue. 这个打开令牌被转义。删除反斜杠并继续。
        builder.append(src, offset, start - offset - 1).append(openToken); // 进行拼接非表达式的值
        offset = start + openToken.length();
      } else { // 不然的话，就发现了占位符
        // found open token. let's search close token. 发现了表达式，开始找到表达式的尾部
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        builder.append(src, offset, start - offset); // 拼接处前面的语句 select * from user where id =
        offset = start + openToken.length();// 计算开头的位置，属于表达式的部分
        int end = text.indexOf(closeToken, offset); // 从offset出开始查找我们的关闭占位符位置
        while (end > -1) {
          if ((end <= offset) || (src[end - 1] != '\\')) {
            expression.append(src, offset, end - offset); //解析出我们占位符的字符
            break;
          }
          // this close token is escaped. remove the backslash and continue.
          expression.append(src, offset, end - offset - 1).append(closeToken);
          offset = end + closeToken.length();
          end = text.indexOf(closeToken, offset); // 在计算表达式尾部的位置
        }
        if (end == -1) {
          // close token was not found.
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          builder.append(handler.handleToken(expression.toString())); // 根据id 解析出我们应该使用什么样的符号进行拼接到sql中,转给对应的解析表达式进行处理
          offset = end + closeToken.length();
        }
      }
      start = text.indexOf(openToken, offset); // 开始查找下一个对应的#{ 占位符
    } while (start > -1);
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
