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
package org.apache.ibatis.scripting.xmltags;

import java.util.regex.Pattern;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

/** 字符串节点 TextSqlNode对象的解析是必要的，因为它能够替换掉其中的“${}”占位符 存在两个内部类 BindingTokenParser类和 DynamicCheckerTokenParser类
 * @author Clinton Begin
 */
public class TextSqlNode implements SqlNode {
  private final String text;
  private final Pattern injectionFilter;

  public TextSqlNode(String text) {
    this(text, null);
  }

  public TextSqlNode(String text, Pattern injectionFilter) {
    this.text = text;
    this.injectionFilter = injectionFilter;
  }

  public boolean isDynamic() { // 判断当前节点是否是动态的， 对于 TextSqlNode对象而言，如果内部含有“${}”占位符，那它就是动态的，否则就不是动态的
    DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser(); // 占位符处理器，只会进行判断是否是动态的，并不会进行处理占位符，处理占位符是另外一个BindingTokenParser
    GenericTokenParser parser = createParser(checker);
    parser.parse(text);
    return checker.isDynamic();
  }

  @Override
  public boolean apply(DynamicContext context) { // 完成该节点的自身的解析
    GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter)); // 常见通用的占位符解析器
    context.appendSql(parser.parse(text)); // 替换掉对应的${} 占位符
    return true;
  }

  private GenericTokenParser createParser(TokenHandler handler) { // 返回我们的表达式处理器
    return new GenericTokenParser("${", "}", handler);
  }
  //
  private static class BindingTokenParser implements TokenHandler {

    private final DynamicContext context;
    private final Pattern injectionFilter;

    public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
      this.context = context;
      this.injectionFilter = injectionFilter;
    }

    @Override
    public String handleToken(String content) { // 取出占位符中的变量，然后使用该变量作为键去上下文环境中寻找对应的值,会用找到的值替换占位符
      Object parameter = context.getBindings().get("_parameter");
      if (parameter == null) {
        context.getBindings().put("value", null);
      } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
        context.getBindings().put("value", parameter);
      }
      Object value = OgnlCache.getValue(content, context.getBindings());
      String srtValue = value == null ? "" : String.valueOf(value); // issue #274 return "" instead of "null"
      checkInjection(srtValue);
      return srtValue;
    }

    private void checkInjection(String value) {
      if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
        throw new ScriptingException("Invalid input. Please conform to regex" + injectionFilter.pattern());
      }
    }
  }

  private static class DynamicCheckerTokenParser implements TokenHandler {

    private boolean isDynamic;

    public DynamicCheckerTokenParser() {
      // Prevent Synthetic Access
    }

    public boolean isDynamic() {
      return isDynamic;
    }

    @Override
    public String handleToken(String content) { //  可以记录自身是否遇到过占位符
      this.isDynamic = true;
      return null;
    }
  }

}
