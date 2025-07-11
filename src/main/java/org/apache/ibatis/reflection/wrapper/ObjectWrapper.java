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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/** 对象包装器的标准规范，将对象包装成Wrapper
 * @author Clinton Begin
 * 一个bean经过BeanWrapper封装后，就可以暴露出如下的大量的易用方法，可以简单的实现对其属性，方法的操作
 * 通过BaseWrapper抽象出公共的方法，然后分别增加了对基本对象，BeanWrapper,MapWrapper,CollectionWrapper的包装
 */
public interface ObjectWrapper {
  // 获得被包装对象的某一个属性的值(解析prop,从被包装的对象中得到值)
  Object get(PropertyTokenizer prop);
  // 设置被包装对象中的某一个属性的值为value
  void set(PropertyTokenizer prop, Object value);
  // 找到对应的属性名称，需要明确是否使用了驼峰命名
  String findProperty(String name, boolean useCamelCaseMapping);
  // 获得所有的属性的get方法名称
  String[] getGetterNames();
  // 获得所有的属性的set方法名称
  String[] getSetterNames();
  // 获得指定属性名称的set方法的类型
  Class<?> getSetterType(String name);
  // 获得指定属性名称的get方法的类型
  Class<?> getGetterType(String name);
  // 判断某一个属性是否存在setter方法
  boolean hasSetter(String name);
  // 判断某一个属性是否存在getter方法
  boolean hasGetter(String name);
  // 实例化某一个属性的值
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);
  // 关于集合的实现
  boolean isCollection();
  // 关于集合的实现
  void add(Object element);
  // 关于集合的实现
  <E> void addAll(List<E> element);

}
