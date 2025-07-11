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
package org.apache.ibatis.builder.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

/** 间接注解映射的解析类  解析@SelectProvider 带provider的注解  实现了sqlSource
 * @author Clinton Begin
 * @author Kazuki Shimizu 通过注解映射的形式获取的sql语句，只有这个实现了sqlSource的是解析注解的，其他的都是解析xml的
 */
public class ProviderSqlSource implements SqlSource {

  private final Configuration configuration;
  private final Class<?> providerType; // 间接注解上type属性所指的类
  private final LanguageDriver languageDriver;  // 语言驱动
  private final Method mapperMethod; // 含有注解的接口方法
  private final Method providerMethod; // @*Provider 注解上method属性所指的方法
  private final String[] providerMethodArgumentNames; // 给定SQL语句的方法对应的参数
  private final Class<?>[] providerMethodParameterTypes; //给定SQL语句的方法对应的参数类型
  private final ProviderContext providerContext; // 信息封装类
  private final Integer providerContextIndex; //  ProviderContext编号

  /**
   * This constructor will remove at a future version.
   *
   * @param configuration
   *          the configuration
   * @param provider
   *          the provider
   *
   * @deprecated Since 3.5.3, Please use the {@link #ProviderSqlSource(Configuration, Annotation, Class, Method)}
   *             instead of this.
   */
  @Deprecated
  public ProviderSqlSource(Configuration configuration, Object provider) {
    this(configuration, provider, null, null);
  }

  /**
   * This constructor will remove at a future version.
   *
   * @param configuration
   *          the configuration
   * @param provider
   *          the provider
   * @param mapperType
   *          the mapper type
   * @param mapperMethod
   *          the mapper method
   *
   * @since 3.4.5
   *
   * @deprecated Since 3.5.3, Please use the {@link #ProviderSqlSource(Configuration, Annotation, Class, Method)}
   *             instead of this.
   */
  @Deprecated
  public ProviderSqlSource(Configuration configuration, Object provider, Class<?> mapperType, Method mapperMethod) {
    this(configuration, (Annotation) provider, mapperType, mapperMethod);
  }

  /**
   * Instantiates a new provider sql source.
   *
   * @param configuration
   *          the configuration
   * @param provider
   *          the provider
   * @param mapperType
   *          the mapper type
   * @param mapperMethod
   *          the mapper method
   *
   * @since 3.5.3
   */
  public ProviderSqlSource(Configuration configuration, Annotation provider, Class<?> mapperType, Method mapperMethod) {
    String candidateProviderMethodName;
    Method candidateProviderMethod = null;
    try {
      this.configuration = configuration;
      this.mapperMethod = mapperMethod;
      Lang lang = mapperMethod == null ? null : mapperMethod.getAnnotation(Lang.class);
      this.languageDriver = configuration.getLanguageDriver(lang == null ? null : lang.value());
      this.providerType = getProviderType(configuration, provider, mapperMethod);
      candidateProviderMethodName = (String) provider.annotationType().getMethod("method").invoke(provider);

      if (candidateProviderMethodName.length() == 0
          && ProviderMethodResolver.class.isAssignableFrom(this.providerType)) {
        candidateProviderMethod = ((ProviderMethodResolver) this.providerType.getDeclaredConstructor().newInstance())
            .resolveMethod(new ProviderContext(mapperType, mapperMethod, configuration.getDatabaseId()));
      }
      if (candidateProviderMethod == null) {
        candidateProviderMethodName = candidateProviderMethodName.length() == 0 ? "provideSql"
            : candidateProviderMethodName;
        for (Method m : this.providerType.getMethods()) {
          if (candidateProviderMethodName.equals(m.getName())
              && CharSequence.class.isAssignableFrom(m.getReturnType())) {
            if (candidateProviderMethod != null) {
              throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                  + candidateProviderMethodName + "' is found multiple in SqlProvider '" + this.providerType.getName()
                  + "'. Sql provider method can not overload.");
            }
            candidateProviderMethod = m;
          }
        }
      }
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
    }
    if (candidateProviderMethod == null) {
      throw new BuilderException("Error creating SqlSource for SqlProvider. Method '" + candidateProviderMethodName
          + "' not found in SqlProvider '" + this.providerType.getName() + "'.");
    }
    this.providerMethod = candidateProviderMethod;
    this.providerMethodArgumentNames = new ParamNameResolver(configuration, this.providerMethod).getNames(); // 使用参数类解析
    this.providerMethodParameterTypes = this.providerMethod.getParameterTypes();

    ProviderContext candidateProviderContext = null;
    Integer candidateProviderContextIndex = null;
    for (int i = 0; i < this.providerMethodParameterTypes.length; i++) {
      Class<?> parameterType = this.providerMethodParameterTypes[i];
      if (parameterType == ProviderContext.class) {
        if (candidateProviderContext != null) {
          throw new BuilderException(
              "Error creating SqlSource for SqlProvider. ProviderContext found multiple in SqlProvider method ("
                  + this.providerType.getName() + "." + providerMethod.getName()
                  + "). ProviderContext can not define multiple in SqlProvider method argument.");
        }
        candidateProviderContext = new ProviderContext(mapperType, mapperMethod, configuration.getDatabaseId());
        candidateProviderContextIndex = i;
      }
    }
    this.providerContext = candidateProviderContext;
    this.providerContextIndex = candidateProviderContextIndex;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) { // 实现的sqlSource接口的方法
    SqlSource sqlSource = createSqlSource(parameterObject); // 得到对应的SqlSource对象
    return sqlSource.getBoundSql(parameterObject); // 从sqlSource中再次得到BoundSql
  }
  // 1.调用*Provider注解的type类中的method方法获得到SQL字符串，2.通过LanguageDriver产生SqlSource对象 3.根据sqlSource对象产生BoundSql
  private SqlSource createSqlSource(Object parameterObject) { //得到我们的sqlSource
    try {
      String sql; //字符串信息
      if (parameterObject instanceof Map) { // 参数为Map
        int bindParameterCount = providerMethodParameterTypes.length - (providerContext == null ? 0 : 1); //得到注解指定的方法的参数的个数
        if (bindParameterCount == 1
            && providerMethodParameterTypes[Integer.valueOf(0).equals(providerContextIndex) ? 1 : 0]
                .isAssignableFrom(parameterObject.getClass())) {
          sql = invokeProviderMethod(extractProviderMethodArguments(parameterObject)); // 调用注解*Provider中指定的方法得到对应的String类型的sql语句
        } else {
          @SuppressWarnings("unchecked")
          Map<String, Object> params = (Map<String, Object>) parameterObject;
          sql = invokeProviderMethod(extractProviderMethodArguments(params, providerMethodArgumentNames));
        }
      } else
        switch (providerMethodParameterTypes.length) { // 注解Type属性指定的参数的个数
          case 0:
            sql = invokeProviderMethod();
            break;
          case 1:
            if (providerContext == null) {
              sql = invokeProviderMethod(parameterObject); //存在一个输入实参
            } else {
              sql = invokeProviderMethod(providerContext); // 输入的参数类型为 providerContext
            }
            break;
          case 2: //两个参数
            sql = invokeProviderMethod(extractProviderMethodArguments(parameterObject));
            break;
          default:
            throw new BuilderException("Cannot invoke SqlProvider method '" + providerMethod
                + "' with specify parameter '" + (parameterObject == null ? null : parameterObject.getClass())
                + "' because SqlProvider method arguments for '" + mapperMethod + "' is an invalid combination.");
        }
      Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
      return languageDriver.createSqlSource(configuration, sql, parameterType); // 使用languageDriver 产生SqlSource
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error invoking SqlProvider method '" + providerMethod + "' with specify parameter '"
          + (parameterObject == null ? null : parameterObject.getClass()) + "'.  Cause: " + extractRootCause(e), e);
    }
  }

  private Throwable extractRootCause(Exception e) {
    Throwable cause = e;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }

  private Object[] extractProviderMethodArguments(Object parameterObject) {
    if (providerContext != null) {
      Object[] args = new Object[2];
      args[providerContextIndex == 0 ? 1 : 0] = parameterObject;
      args[providerContextIndex] = providerContext;
      return args;
    }
    return new Object[] { parameterObject };
  }

  private Object[] extractProviderMethodArguments(Map<String, Object> params, String[] argumentNames) {
    Object[] args = new Object[argumentNames.length];
    for (int i = 0; i < args.length; i++) {
      if (providerContextIndex != null && providerContextIndex == i) {
        args[i] = providerContext;
      } else {
        args[i] = params.get(argumentNames[i]);
      }
    }
    return args;
  }

  private String invokeProviderMethod(Object... args) throws Exception {
    Object targetObject = null;
    if (!Modifier.isStatic(providerMethod.getModifiers())) {
      targetObject = providerType.getDeclaredConstructor().newInstance();
    }
    CharSequence sql = (CharSequence) providerMethod.invoke(targetObject, args);
    return sql != null ? sql.toString() : null;
  }

  private Class<?> getProviderType(Configuration configuration, Annotation providerAnnotation, Method mapperMethod)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> type = (Class<?>) providerAnnotation.annotationType().getMethod("type").invoke(providerAnnotation);
    Class<?> value = (Class<?>) providerAnnotation.annotationType().getMethod("value").invoke(providerAnnotation);
    if (value == void.class && type == void.class) {
      if (configuration.getDefaultSqlProviderType() != null) {
        return configuration.getDefaultSqlProviderType();
      }
      throw new BuilderException("Please specify either 'value' or 'type' attribute of @"
          + providerAnnotation.annotationType().getSimpleName() + " at the '" + mapperMethod.toString() + "'.");
    }
    if (value != void.class && type != void.class && value != type) {
      throw new BuilderException("Cannot specify different class on 'value' and 'type' attribute of @"
          + providerAnnotation.annotationType().getSimpleName() + " at the '" + mapperMethod.toString() + "'.");
    }
    return value == void.class ? type : value;
  }

}
