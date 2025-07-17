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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

import ognl.MemberAccess;
import ognl.OgnlContext;

import org.apache.ibatis.reflection.Reflector;

/** 实现了 MemberAccess接口，并基于反射提供了修改对象属性可访问性的功能  ，OGNL便可以基于这些功能为访问对象的属性做好准备
 * The {@link MemberAccess} class that based on <a href=
 * 'https://github.com/jkuhnert/ognl/blob/OGNL_3_2_1/src/java/ognl/DefaultMemberAccess.java'>DefaultMemberAccess</a>.
 *
 * @author Kazuki Shimizu
 *
 * @since 3.5.0
 *
 * @see <a href=
 *      'https://github.com/jkuhnert/ognl/blob/OGNL_3_2_1/src/java/ognl/DefaultMemberAccess.java'>DefaultMemberAccess</a>
 * @see <a href='https://github.com/jkuhnert/ognl/issues/47'>#47 of ognl</a>
 */
class OgnlMemberAccess implements MemberAccess {
  // 当前环境下，通过反射是否能够修改对象属性的可访问性
  private final boolean canControlMemberAccessible;

  OgnlMemberAccess() {
    this.canControlMemberAccessible = Reflector.canControlMemberAccessible(); // 通过Reflector工具类进行处理
  }

  @Override //设置属性的可访问性，在环境上下文context中,目标对象target 目标对象的目标成员member 属性名称propertyName
  public Object setup(OgnlContext context, Object target, Member member, String propertyName) {
    Object result = null;
    if (isAccessible(context, target, member, propertyName)) { //如果允许修改属性的可访问性
      AccessibleObject accessible = (AccessibleObject) member;
      if (!accessible.isAccessible()) { // 原本属性不可以访问的话，进行修改为可访问
        result = Boolean.FALSE;
        accessible.setAccessible(true);
      }
    }
    return result; // 返回属性的可访问性
  }

  @Override // 将属性的可访问性恢复到指定状态 环境上下文context中,目标对象target 目标对象的目标成员member 属性名称propertyName
  public void restore(OgnlContext context, Object target, Member member, String propertyName, Object state) {
    // Flipping accessible flag is not thread safe. See #1648
  }

  @Override // 判断对象属性是否可访问
  public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
    return canControlMemberAccessible;
  }

}
