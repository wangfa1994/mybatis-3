/*
 *    Copyright 2009-2024 the original author or authors.
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

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 */
public class BeanWrapper extends BaseWrapper {

  private final Object object; // 被包装的对象 (从父类会继承出来一个MetaObject对象，每一个对象都会存在一个元信息对象)
  private final MetaClass metaClass; //被包装对象属于类的元类

  public BeanWrapper(MetaObject metaObject, Object object) {
    super(metaObject);
    this.object = object;
    this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    if (prop.hasNext()) {
      return getChildValue(prop);
    } else if (prop.getIndex() != null) {
      return getCollectionValue(prop, resolveCollection(prop, object));
    } else {
      return getBeanProperty(prop, object);
    }
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    if (prop.hasNext()) {
      setChildValue(prop, value);
    } else if (prop.getIndex() != null) {
      setCollectionValue(prop, resolveCollection(prop, object), value);
    } else {
      setBeanProperty(prop, object, value);
    }
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    return metaClass.findProperty(name, useCamelCaseMapping);
  }

  @Override
  public String[] getGetterNames() {
    return metaClass.getGetterNames();
  }

  @Override
  public String[] getSetterNames() {
    return metaClass.getSetterNames();
  }

  @Override
  public Class<?> getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return metaClass.getSetterType(name);
    }
    MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
    if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
      return metaClass.getSetterType(name);
    }
    return metaValue.getSetterType(prop.getChildren());
  }

  @Override
  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return metaClass.getGetterType(name);
    }
    MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
    if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
      return metaClass.getGetterType(name);
    }
    return metaValue.getGetterType(prop.getChildren());
  }

  @Override
  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return metaClass.hasSetter(name);
    }
    if (metaClass.hasSetter(prop.getIndexedName())) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return metaClass.hasSetter(name);
      }
      return metaValue.hasSetter(prop.getChildren());
    }
    return false;
  }

  @Override
  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return metaClass.hasGetter(name);
    }
    if (metaClass.hasGetter(prop.getIndexedName())) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return metaClass.hasGetter(name);
      }
      return metaValue.hasGetter(prop.getChildren());
    }
    return false;
  }

  @Override
  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    MetaObject metaValue;
    Class<?> type = getSetterType(prop.getName());
    try {
      Object newObject = objectFactory.create(type);
      metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(),
          metaObject.getReflectorFactory());
      set(prop, newObject);
    } catch (Exception e) {
      throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name
          + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
    }
    return metaValue;
  }

  private Object getBeanProperty(PropertyTokenizer prop, Object object) {
    try {
      Invoker method = metaClass.getGetInvoker(prop.getName());
      try {
        return method.invoke(object, NO_ARGUMENTS);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      throw new ReflectionException(
          "Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
    }
  }

  private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
    try {
      Invoker method = metaClass.getSetInvoker(prop.getName());
      Object[] params = { value };
      try {
        method.invoke(object, params);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (Throwable t) {
      throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass()
          + "' with value '" + value + "' Cause: " + t.toString(), t);
    }
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public void add(Object element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> void addAll(List<E> list) {
    throw new UnsupportedOperationException();
  }

}
