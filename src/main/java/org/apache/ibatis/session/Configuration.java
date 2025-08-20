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
package org.apache.ibatis.session;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.datasource.jndi.JndiDataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/** mybatis的配置文件会被封装成 此类 Configuration
 * @author Clinton Begin
 */ //(properties?, settings?, typeAliases?, typeHandlers?, objectFactory?, objectWrapperFactory?, reflectorFactory?, plugins?, environments?, databaseIdProvider?, mappers?)
public class Configuration {

  protected Environment environment; //configuraton标签中的子标签 环境标签environments

  protected boolean safeRowBoundsEnabled; // settings标签中的子标签
  protected boolean safeResultHandlerEnabled = true;  // settings标签中的子标签
  protected boolean mapUnderscoreToCamelCase; // settings标签中的子标签
  protected boolean aggressiveLazyLoading; // settings标签中的子标签
  protected boolean useGeneratedKeys; // settings标签中的子标签
  protected boolean useColumnLabel = true; // settings标签中的子标签
  protected boolean cacheEnabled = true; // settings标签中的子标签   二级缓存配置属性
  protected boolean callSettersOnNulls; // settings标签中的子标签
  protected boolean useActualParamName = true; // settings标签中的子标签 这个主要是处理mapper接口中方法的参数是否使用了真实形参进行处理，默认是使用了，可以进行没有标注@paramc参数的解析
  protected boolean returnInstanceForEmptyRow; // settings标签中的子标签
  protected boolean shrinkWhitespacesInSql; // settings标签中的子标签
  protected boolean nullableOnForEach; // settings标签中的子标签
  protected boolean argNameBasedConstructorAutoMapping; // settings标签中的子标签

  protected String logPrefix; // settings标签中的子标签
  protected Class<? extends Log> logImpl; //settings标签中的子标签日志实现类
  protected Class<? extends VFS> vfsImpl; // settings标签中的子标签
  protected Class<?> defaultSqlProviderType; // settings标签中的子标签
  protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION; // settings标签中的子标签  一级缓存本地默认为session级别的，如果我们修改了为Statement级别的，我们则会进行清空缓存
  protected JdbcType jdbcTypeForNull = JdbcType.OTHER;  // settings标签中的子标签
  protected Set<String> lazyLoadTriggerMethods = new HashSet<>(
      Arrays.asList("equals", "clone", "hashCode", "toString")); //  settings标签中的子标签 lazyLoadTriggerMethods
  protected Integer defaultStatementTimeout; // settings标签中的子标签
  protected Integer defaultFetchSize; // settings标签中的子标签
  protected ResultSetType defaultResultSetType; // settings标签中的子标签
  protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE; // settings标签中的子标签
  protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;  // settings标签中的子标签
  protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE; // settings标签中的子标签

  protected Properties variables = new Properties(); //properties标签节点信息 通过resource或者url解析出来的properties数据存放，jdbc的配置属性 配置文件的properties标签内容存放的变量
  protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory(); // 解析我们的 reflectorFactory 标签存放值 反射工厂
  protected ObjectFactory objectFactory = new DefaultObjectFactory(); // 解析出来我们的objectFactory标签存放的值，用来创建我们的对象  对象工厂
  protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory(); // 解析出来我们的ObjectWrapperFactory标签存放值 对象包装工厂

  protected boolean lazyLoadingEnabled; // settings标签中的子标签 是否启用懒加载
  protected ProxyFactory proxyFactory = new JavassistProxyFactory(); // #224 Using internal Javassist instead of OGNL  settings标签中的子标签 代理工厂，这个代理工厂在哪里使用了呢？ 这个好像没用

  protected String databaseId; // configuration标签中的子标签 数据库编号
  /**
   * Configuration factory class. Used to create Configuration for loading deserialized unread properties.
   *
   * @see <a href='https://github.com/mybatis/old-google-code-issues/issues/300'>Issue 300 (google code)</a>
   */
  protected Class<?> configurationFactory; // settings标签中的子标签 配置工厂，用来创建用于加载反序列的未读属性的配置

  protected final MapperRegistry mapperRegistry = new MapperRegistry(this); //configuration标签下的子标签  mapperRegistry注册器， 里面封装了我们对应的MapperProxyFactory,当我们的mapper资源通过package进行引入的话，会进行这个对象的赋值 映射注册表
  protected final InterceptorChain interceptorChain = new InterceptorChain(); // 存放我们的插件链，解析的plugin标签会被封装到这里 连接器链，用来支持插件的插入
  protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this); // configuration标签下的子标签，类型解析器 ，// settings标签中的子标签的结果会被放置到这个对象中的 defaultEnumTypeHandler //类型处理器注册表
  protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry(); // 别名注册器，configuration对象和TypeAliasRegistry对象初始化的时候进行了内部别名的定义， 还有我们自定义通过typeAliases标签进行定义的，用于简化我们的sql片段 //类型别名注册表
  protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry(); //sql的语言驱动器的注册器，用来管理我们的LanguageDriver，则用来解析我们的sql //语言驱动注册表
  // mappedStatements 用来存放我们的命名空间+id 唯一的值，这个是操作数据库语句的唯一编号,StrictMap,key 不能被覆盖的map // 映射的数据库操作语句
  protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>(
      "Mapped Statements collection")
          .conflictMessageProducer((savedValue, targetValue) -> ". please check " + savedValue.getResource() + " and "
              + targetValue.getResource()); // mapper文件中的增删改查标签解析出来的对象存放处
  protected final Map<String, Cache> caches = new StrictMap<>("Caches collection"); // 解析mapper下中的cache标签  如果开启了二级缓存，各个mapper下的缓存策略 //缓存
  protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection"); // mapper文件中的resultMap标签的解析存放值 //结果映射
  protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection"); // mapper文件中的parameterMap标签的解析存放值,每一个 //参数映射
  protected final Map<String, KeyGenerator> keyGenerators = new StrictMap<>("Key Generators collection"); //statement类型语句对应的KeyGenerator,在解析增删改查语句的时候会进行放置 // 主键生成器

  protected final Set<String> loadedResources = new HashSet<>(); // 已经处理过的资源集合 //载入的资源 如映射文件资源
  protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers"); // mapper文件中标签sql片段的存储，在创建xmlMapperBuilder的时候进行传递 sql节点
  protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>(); // 存放暂时性错误的结点
  protected final Collection<CacheRefResolver> incompleteCacheRefs = new LinkedList<>(); // mapper文件中的标签cache-ref 存放 这个存在异常的时候放值
  protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>(); // 暂时性存放临时性的错误的结点，因为可能存在依赖关系，而依赖关系的结点还没有处理
  protected final Collection<MethodResolver> incompleteMethods = new LinkedList<>(); //存放暂时性错误的结点。第一轮解析完之后，进行第二轮错误节点的处理的时候，就会触发这些节点的调用，再次完成解析
  //方案一：第一轮解析节点和处理依赖，将异常节点进行缓存，第二轮再进行解析一次，再次处理依赖关系， 方案二：第一轮解析所有的结点，不处理依赖关系，在第二轮解析的时候只处理依赖关系(spring模式)
  private final ReentrantLock incompleteResultMapsLock = new ReentrantLock();
  private final ReentrantLock incompleteCacheRefsLock = new ReentrantLock();
  private final ReentrantLock incompleteStatementsLock = new ReentrantLock();
  private final ReentrantLock incompleteMethodsLock = new ReentrantLock();

  /*
   * A map holds cache-ref relationship. The key is the namespace that references a cache bound to another namespace and
   * the value is the namespace which the actual cache is bound to.
   */
  protected final Map<String, String> cacheRefMap = new HashMap<>(); // mapper文件中的标签cache-ref 存放 //用来存储跨namespace的缓存共享设置

  public Configuration(Environment environment) {
    this();
    this.environment = environment;
  }

  public Configuration() { // 在Configuration对象进行实例化的时候，也进行了一些类型的注册 typeAliasRegistry
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);

    typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

    typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
    typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
    typeAliasRegistry.registerAlias("LRU", LruCache.class);
    typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
    typeAliasRegistry.registerAlias("WEAK", WeakCache.class);

    typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

    typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
    typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);

    typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
    typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
    typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
    typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
    typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
    typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
    typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

    typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
    typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

    languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    languageRegistry.register(RawLanguageDriver.class);
  }

  public String getLogPrefix() {
    return logPrefix;
  }

  public void setLogPrefix(String logPrefix) {
    this.logPrefix = logPrefix;
  }

  public Class<? extends Log> getLogImpl() {
    return logImpl;
  }

  public void setLogImpl(Class<? extends Log> logImpl) {
    if (logImpl != null) {
      this.logImpl = logImpl;
      LogFactory.useCustomLogging(this.logImpl);
    }
  }

  public Class<? extends VFS> getVfsImpl() {
    return this.vfsImpl;
  }

  public void setVfsImpl(Class<? extends VFS> vfsImpl) {
    if (vfsImpl != null) {
      this.vfsImpl = vfsImpl;
      VFS.addImplClass(this.vfsImpl);
    }
  }

  /**
   * Gets an applying type when omit a type on sql provider annotation(e.g.
   * {@link org.apache.ibatis.annotations.SelectProvider}).
   *
   * @return the default type for sql provider annotation
   *
   * @since 3.5.6
   */
  public Class<?> getDefaultSqlProviderType() {
    return defaultSqlProviderType;
  }

  /**
   * Sets an applying type when omit a type on sql provider annotation(e.g.
   * {@link org.apache.ibatis.annotations.SelectProvider}).
   *
   * @param defaultSqlProviderType
   *          the default type for sql provider annotation
   *
   * @since 3.5.6
   */
  public void setDefaultSqlProviderType(Class<?> defaultSqlProviderType) {
    this.defaultSqlProviderType = defaultSqlProviderType;
  }

  public boolean isCallSettersOnNulls() {
    return callSettersOnNulls;
  }

  public void setCallSettersOnNulls(boolean callSettersOnNulls) {
    this.callSettersOnNulls = callSettersOnNulls;
  }

  public boolean isUseActualParamName() {
    return useActualParamName;
  }

  public void setUseActualParamName(boolean useActualParamName) {
    this.useActualParamName = useActualParamName;
  }

  public boolean isReturnInstanceForEmptyRow() {
    return returnInstanceForEmptyRow;
  }

  public void setReturnInstanceForEmptyRow(boolean returnEmptyInstance) {
    this.returnInstanceForEmptyRow = returnEmptyInstance;
  }

  public boolean isShrinkWhitespacesInSql() {
    return shrinkWhitespacesInSql;
  }

  public void setShrinkWhitespacesInSql(boolean shrinkWhitespacesInSql) {
    this.shrinkWhitespacesInSql = shrinkWhitespacesInSql;
  }

  /**
   * Sets the default value of 'nullable' attribute on 'foreach' tag.
   *
   * @param nullableOnForEach
   *          If nullable, set to {@code true}
   *
   * @since 3.5.9
   */
  public void setNullableOnForEach(boolean nullableOnForEach) {
    this.nullableOnForEach = nullableOnForEach;
  }

  /**
   * Returns the default value of 'nullable' attribute on 'foreach' tag.
   * <p>
   * Default is {@code false}.
   *
   * @return If nullable, set to {@code true}
   *
   * @since 3.5.9
   */
  public boolean isNullableOnForEach() {
    return nullableOnForEach;
  }

  public boolean isArgNameBasedConstructorAutoMapping() {
    return argNameBasedConstructorAutoMapping;
  }

  public void setArgNameBasedConstructorAutoMapping(boolean argNameBasedConstructorAutoMapping) {
    this.argNameBasedConstructorAutoMapping = argNameBasedConstructorAutoMapping;
  }

  public String getDatabaseId() {
    return databaseId;
  }

  public void setDatabaseId(String databaseId) {
    this.databaseId = databaseId;
  }

  public Class<?> getConfigurationFactory() {
    return configurationFactory;
  }

  public void setConfigurationFactory(Class<?> configurationFactory) {
    this.configurationFactory = configurationFactory;
  }

  public boolean isSafeResultHandlerEnabled() {
    return safeResultHandlerEnabled;
  }

  public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
    this.safeResultHandlerEnabled = safeResultHandlerEnabled;
  }

  public boolean isSafeRowBoundsEnabled() {
    return safeRowBoundsEnabled;
  }

  public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
    this.safeRowBoundsEnabled = safeRowBoundsEnabled;
  }

  public boolean isMapUnderscoreToCamelCase() {
    return mapUnderscoreToCamelCase;
  }

  public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
    this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
  }

  public void addLoadedResource(String resource) {
    loadedResources.add(resource);
  }

  public boolean isResourceLoaded(String resource) {
    return loadedResources.contains(resource);
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public AutoMappingBehavior getAutoMappingBehavior() {
    return autoMappingBehavior;
  }

  public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
    this.autoMappingBehavior = autoMappingBehavior;
  }

  /**
   * Gets the auto mapping unknown column behavior.
   *
   * @return the auto mapping unknown column behavior
   *
   * @since 3.4.0
   */
  public AutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
    return autoMappingUnknownColumnBehavior;
  }

  /**
   * Sets the auto mapping unknown column behavior.
   *
   * @param autoMappingUnknownColumnBehavior
   *          the new auto mapping unknown column behavior
   *
   * @since 3.4.0
   */
  public void setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
    this.autoMappingUnknownColumnBehavior = autoMappingUnknownColumnBehavior;
  }

  public boolean isLazyLoadingEnabled() {
    return lazyLoadingEnabled;
  }

  public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
    this.lazyLoadingEnabled = lazyLoadingEnabled;
  }

  public ProxyFactory getProxyFactory() {
    return proxyFactory;
  }

  public void setProxyFactory(ProxyFactory proxyFactory) {
    if (proxyFactory == null) {
      proxyFactory = new JavassistProxyFactory();
    }
    this.proxyFactory = proxyFactory;
  }

  public boolean isAggressiveLazyLoading() {
    return aggressiveLazyLoading;
  }

  public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
    this.aggressiveLazyLoading = aggressiveLazyLoading;
  }

  /**
   * @deprecated You can safely remove the call to this method as this option had no effect.
   */
  @Deprecated
  public boolean isMultipleResultSetsEnabled() {
    return true;
  }

  /**
   * @deprecated You can safely remove the call to this method as this option had no effect.
   */
  @Deprecated
  public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
    // nop
  }

  public Set<String> getLazyLoadTriggerMethods() {
    return lazyLoadTriggerMethods;
  }

  public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
    this.lazyLoadTriggerMethods = lazyLoadTriggerMethods;
  }

  public boolean isUseGeneratedKeys() {
    return useGeneratedKeys;
  }

  public void setUseGeneratedKeys(boolean useGeneratedKeys) {
    this.useGeneratedKeys = useGeneratedKeys;
  }

  public ExecutorType getDefaultExecutorType() {
    return defaultExecutorType;
  }

  public void setDefaultExecutorType(ExecutorType defaultExecutorType) {
    this.defaultExecutorType = defaultExecutorType;
  }

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public Integer getDefaultStatementTimeout() {
    return defaultStatementTimeout;
  }

  public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
    this.defaultStatementTimeout = defaultStatementTimeout;
  }

  /**
   * Gets the default fetch size.
   *
   * @return the default fetch size
   *
   * @since 3.3.0
   */
  public Integer getDefaultFetchSize() {
    return defaultFetchSize;
  }

  /**
   * Sets the default fetch size.
   *
   * @param defaultFetchSize
   *          the new default fetch size
   *
   * @since 3.3.0
   */
  public void setDefaultFetchSize(Integer defaultFetchSize) {
    this.defaultFetchSize = defaultFetchSize;
  }

  /**
   * Gets the default result set type.
   *
   * @return the default result set type
   *
   * @since 3.5.2
   */
  public ResultSetType getDefaultResultSetType() {
    return defaultResultSetType;
  }

  /**
   * Sets the default result set type.
   *
   * @param defaultResultSetType
   *          the new default result set type
   *
   * @since 3.5.2
   */
  public void setDefaultResultSetType(ResultSetType defaultResultSetType) {
    this.defaultResultSetType = defaultResultSetType;
  }

  public boolean isUseColumnLabel() {
    return useColumnLabel;
  }

  public void setUseColumnLabel(boolean useColumnLabel) {
    this.useColumnLabel = useColumnLabel;
  }

  public LocalCacheScope getLocalCacheScope() {
    return localCacheScope;
  }

  public void setLocalCacheScope(LocalCacheScope localCacheScope) {
    this.localCacheScope = localCacheScope;
  }

  public JdbcType getJdbcTypeForNull() {
    return jdbcTypeForNull;
  }

  public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
    this.jdbcTypeForNull = jdbcTypeForNull;
  }

  public Properties getVariables() {
    return variables;
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  public TypeHandlerRegistry getTypeHandlerRegistry() {
    return typeHandlerRegistry;
  }

  /**
   * Set a default {@link TypeHandler} class for {@link Enum}. A default {@link TypeHandler} is
   * {@link org.apache.ibatis.type.EnumTypeHandler}.
   *
   * @param typeHandler
   *          a type handler class for {@link Enum}
   *
   * @since 3.4.5
   */
  public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
    if (typeHandler != null) {
      getTypeHandlerRegistry().setDefaultEnumTypeHandler(typeHandler);
    }
  }

  public TypeAliasRegistry getTypeAliasRegistry() {
    return typeAliasRegistry;
  }

  /**
   * Gets the mapper registry.
   *
   * @return the mapper registry
   *
   * @since 3.2.2
   */
  public MapperRegistry getMapperRegistry() {
    return mapperRegistry;
  }

  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  public void setReflectorFactory(ReflectorFactory reflectorFactory) {
    this.reflectorFactory = reflectorFactory;
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public void setObjectFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
    this.objectWrapperFactory = objectWrapperFactory;
  }

  /**
   * Gets the interceptors.
   *
   * @return the interceptors
   *
   * @since 3.2.2
   */
  public List<Interceptor> getInterceptors() {
    return interceptorChain.getInterceptors();
  }

  public LanguageDriverRegistry getLanguageRegistry() {
    return languageRegistry;
  }

  public void setDefaultScriptingLanguage(Class<? extends LanguageDriver> driver) {
    if (driver == null) {
      driver = XMLLanguageDriver.class;
    }
    getLanguageRegistry().setDefaultDriverClass(driver);
  }

  public LanguageDriver getDefaultScriptingLanguageInstance() {
    return languageRegistry.getDefaultDriver();
  }

  /**
   * Gets the language driver.
   *
   * @param langClass
   *          the lang class
   *
   * @return the language driver
   *
   * @since 3.5.1
   */
  public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
    if (langClass == null) {
      return languageRegistry.getDefaultDriver();
    }
    languageRegistry.register(langClass);
    return languageRegistry.getDriver(langClass);
  }

  /**
   * Gets the default scripting language instance.
   *
   * @return the default scripting language instance
   *
   * @deprecated Use {@link #getDefaultScriptingLanguageInstance()}
   */
  @Deprecated
  public LanguageDriver getDefaultScriptingLanuageInstance() {
    return getDefaultScriptingLanguageInstance();
  }

  public MetaObject newMetaObject(Object object) { // 获得当前对象的元对象
    return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject,
      BoundSql boundSql) {
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement,
        parameterObject, boundSql);
    return (ParameterHandler) interceptorChain.pluginAll(parameterHandler); // 在创建ParameterHandler的时候进行拦截器的添加
  }

  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds,
      ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler,
        resultHandler, boundSql, rowBounds);
    return (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
  }

  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement,
      Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject,
        rowBounds, resultHandler, boundSql); // 创建出来一个路由RoutingStatementHandler，然后得到对应的真正StatementHandler
    return (StatementHandler) interceptorChain.pluginAll(statementHandler);
  }

  public Executor newExecutor(Transaction transaction) {
    return newExecutor(transaction, defaultExecutorType);
  }

  public Executor newExecutor(Transaction transaction, ExecutorType executorType) { // 创建我们的执行器，最后用于执行我们的sql相关
    executorType = executorType == null ? defaultExecutorType : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) { // 根据数据库的操作类型来创建实际的执行器
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) { // 开启二级缓存，进行包装  默认开启二级缓存
      executor = new CachingExecutor(executor);// 如果开启了二级缓存，再进行装饰一下，先用CachingExecutor缓存处理，再用包装的executor处理
    }
    return (Executor) interceptorChain.pluginAll(executor); // 为执行器增加拦截器插件，以启用各个拦截器的功能
  }

  public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
    keyGenerators.put(id, keyGenerator);
  }

  public Collection<String> getKeyGeneratorNames() {
    return keyGenerators.keySet();
  }

  public Collection<KeyGenerator> getKeyGenerators() {
    return keyGenerators.values();
  }

  public KeyGenerator getKeyGenerator(String id) {
    return keyGenerators.get(id);
  }

  public boolean hasKeyGenerator(String id) {
    return keyGenerators.containsKey(id);
  }

  public void addCache(Cache cache) {
    caches.put(cache.getId(), cache);
  }

  public Collection<String> getCacheNames() {
    return caches.keySet();
  }

  public Collection<Cache> getCaches() {
    return caches.values();
  }

  public Cache getCache(String id) {
    return caches.get(id);
  }

  public boolean hasCache(String id) {
    return caches.containsKey(id);
  }

  public void addResultMap(ResultMap rm) {
    resultMaps.put(rm.getId(), rm);
    checkLocallyForDiscriminatedNestedResultMaps(rm);
    checkGloballyForDiscriminatedNestedResultMaps(rm);
  }

  public Collection<String> getResultMapNames() {
    return resultMaps.keySet();
  }

  public Collection<ResultMap> getResultMaps() {
    return resultMaps.values();
  }

  public ResultMap getResultMap(String id) {
    return resultMaps.get(id);
  }

  public boolean hasResultMap(String id) {
    return resultMaps.containsKey(id);
  }

  public void addParameterMap(ParameterMap pm) {
    parameterMaps.put(pm.getId(), pm);
  }

  public Collection<String> getParameterMapNames() {
    return parameterMaps.keySet();
  }

  public Collection<ParameterMap> getParameterMaps() {
    return parameterMaps.values();
  }

  public ParameterMap getParameterMap(String id) {
    return parameterMaps.get(id);
  }

  public boolean hasParameterMap(String id) {
    return parameterMaps.containsKey(id);
  }

  public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms); //存放命名空间和解析出来的mappedStatement
  }

  public Collection<String> getMappedStatementNames() {
    buildAllStatements();
    return mappedStatements.keySet();
  }

  public Collection<MappedStatement> getMappedStatements() {
    buildAllStatements();
    return mappedStatements.values();
  }

  /**
   * @deprecated call {@link #parsePendingStatements(boolean)}
   */
  @Deprecated
  public Collection<XMLStatementBuilder> getIncompleteStatements() {
    return incompleteStatements;
  }

  public void addIncompleteStatement(XMLStatementBuilder incompleteStatement) {
    incompleteStatementsLock.lock();
    try {
      incompleteStatements.add(incompleteStatement);
    } finally {
      incompleteStatementsLock.unlock();
    }
  }

  /**
   * @deprecated call {@link #parsePendingCacheRefs(boolean)}
   */
  @Deprecated
  public Collection<CacheRefResolver> getIncompleteCacheRefs() {
    return incompleteCacheRefs;
  }

  public void addIncompleteCacheRef(CacheRefResolver incompleteCacheRef) {
    incompleteCacheRefsLock.lock();
    try {
      incompleteCacheRefs.add(incompleteCacheRef);
    } finally {
      incompleteCacheRefsLock.unlock();
    }
  }

  /**
   * @deprecated call {@link #parsePendingResultMaps(boolean)}
   */
  @Deprecated
  public Collection<ResultMapResolver> getIncompleteResultMaps() {
    return incompleteResultMaps;
  }

  public void addIncompleteResultMap(ResultMapResolver resultMapResolver) {
    incompleteResultMapsLock.lock();
    try {
      incompleteResultMaps.add(resultMapResolver);
    } finally {
      incompleteResultMapsLock.unlock();
    }
  }

  public void addIncompleteMethod(MethodResolver builder) {
    incompleteMethodsLock.lock();
    try {
      incompleteMethods.add(builder);
    } finally {
      incompleteMethodsLock.unlock();
    }
  }

  /**
   * @deprecated call {@link #parsePendingMethods(boolean)}
   */
  @Deprecated
  public Collection<MethodResolver> getIncompleteMethods() {
    return incompleteMethods;
  }

  public MappedStatement getMappedStatement(String id) {
    return this.getMappedStatement(id, true);
  }

  public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
    if (validateIncompleteStatements) {
      buildAllStatements();
    }
    return mappedStatements.get(id); // 从我们的mappedStatements中得到我们的mappedStatement
  }

  public Map<String, XNode> getSqlFragments() {
    return sqlFragments;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
  }

  public void addMappers(String packageName, Class<?> superType) {
    mapperRegistry.addMappers(packageName, superType);
  }

  public void addMappers(String packageName) {
    mapperRegistry.addMappers(packageName);
  }

  public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) { // 根据类型得到我们对应的代理对象，代理对象中包括配置信息和sqlSession
    return mapperRegistry.getMapper(type, sqlSession);
  }

  public boolean hasMapper(Class<?> type) {
    return mapperRegistry.hasMapper(type);
  }

  public boolean hasStatement(String statementName) {
    return hasStatement(statementName, true);
  }

  public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
    if (validateIncompleteStatements) {
      buildAllStatements();
    }
    return mappedStatements.containsKey(statementName);
  }

  public void addCacheRef(String namespace, String referencedNamespace) {
    cacheRefMap.put(namespace, referencedNamespace);
  }

  /*
   * Parses all the unprocessed statement nodes in the cache. It is recommended to call this method once all the mappers
   * are added as it provides fail-fast statement validation.
   */
  protected void buildAllStatements() {
    parsePendingResultMaps(true);
    parsePendingCacheRefs(true);
    parsePendingStatements(true);
    parsePendingMethods(true);
  }

  public void parsePendingMethods(boolean reportUnresolved) {
    if (incompleteMethods.isEmpty()) {
      return;
    }
    incompleteMethodsLock.lock();
    try {
      incompleteMethods.removeIf(x -> {
        x.resolve();
        return true;
      });
    } catch (IncompleteElementException e) {
      if (reportUnresolved) {
        throw e;
      }
    } finally {
      incompleteMethodsLock.unlock();
    }
  }

  public void parsePendingStatements(boolean reportUnresolved) {
    if (incompleteStatements.isEmpty()) {
      return;
    }
    incompleteStatementsLock.lock();
    try {
      incompleteStatements.removeIf(x -> {
        x.parseStatementNode();
        return true;
      });
    } catch (IncompleteElementException e) {
      if (reportUnresolved) {
        throw e;
      }
    } finally {
      incompleteStatementsLock.unlock();
    }
  }

  public void parsePendingCacheRefs(boolean reportUnresolved) {
    if (incompleteCacheRefs.isEmpty()) {
      return;
    }
    incompleteCacheRefsLock.lock();
    try {
      incompleteCacheRefs.removeIf(x -> x.resolveCacheRef() != null);
    } catch (IncompleteElementException e) {
      if (reportUnresolved) {
        throw e;
      }
    } finally {
      incompleteCacheRefsLock.unlock();
    }
  }

  public void parsePendingResultMaps(boolean reportUnresolved) {
    if (incompleteResultMaps.isEmpty()) {
      return;
    }
    incompleteResultMapsLock.lock();
    try {
      boolean resolved;
      IncompleteElementException ex = null;
      do {
        resolved = false;
        Iterator<ResultMapResolver> iterator = incompleteResultMaps.iterator();
        while (iterator.hasNext()) {
          try {
            iterator.next().resolve();
            iterator.remove();
            resolved = true;
          } catch (IncompleteElementException e) {
            ex = e;
          }
        }
      } while (resolved);
      if (reportUnresolved && !incompleteResultMaps.isEmpty() && ex != null) {
        // At least one result map is unresolvable.
        throw ex;
      }
    } finally {
      incompleteResultMapsLock.unlock();
    }
  }

  /**
   * Extracts namespace from fully qualified statement id.
   *
   * @param statementId
   *          the statement id
   *
   * @return namespace or null when id does not contain period.
   */
  protected String extractNamespace(String statementId) {
    int lastPeriod = statementId.lastIndexOf('.');
    return lastPeriod > 0 ? statementId.substring(0, lastPeriod) : null;
  }

  // Slow but a one time cost. A better solution is welcome.
  protected void checkGloballyForDiscriminatedNestedResultMaps(ResultMap rm) {
    if (rm.hasNestedResultMaps()) {
      final String resultMapId = rm.getId();
      for (Object resultMapObject : resultMaps.values()) {
        if (resultMapObject instanceof ResultMap) {
          ResultMap entryResultMap = (ResultMap) resultMapObject;
          if (!entryResultMap.hasNestedResultMaps() && entryResultMap.getDiscriminator() != null) {
            Collection<String> discriminatedResultMapNames = entryResultMap.getDiscriminator().getDiscriminatorMap()
                .values();
            if (discriminatedResultMapNames.contains(resultMapId)) {
              entryResultMap.forceNestedResultMaps();
            }
          }
        }
      }
    }
  }

  // Slow but a one time cost. A better solution is welcome.
  protected void checkLocallyForDiscriminatedNestedResultMaps(ResultMap rm) {
    if (!rm.hasNestedResultMaps() && rm.getDiscriminator() != null) {
      for (String discriminatedResultMapName : rm.getDiscriminator().getDiscriminatorMap().values()) {
        if (hasResultMap(discriminatedResultMapName)) {
          ResultMap discriminatedResultMap = resultMaps.get(discriminatedResultMapName);
          if (discriminatedResultMap.hasNestedResultMaps()) {
            rm.forceNestedResultMaps();
            break;
          }
        }
      }
    }
  }
  // key 不能被覆盖写的一个Map，并且值不能为null ， ConcurrentHashMap的类
  protected static class StrictMap<V> extends ConcurrentHashMap<String, V> {

    private static final long serialVersionUID = -4950446264854982944L;
    private final String name;
    private BiFunction<V, V, String> conflictMessageProducer;

    public StrictMap(String name, int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.name = name;
    }

    public StrictMap(String name, int initialCapacity) {
      super(initialCapacity);
      this.name = name;
    }

    public StrictMap(String name) {
      this.name = name;
    }

    public StrictMap(String name, Map<String, ? extends V> m) {
      super(m);
      this.name = name;
    }

    /**  指定一个函数，用于在包含具有相同键的值时产生冲突错误消息。
     * Assign a function for producing a conflict error message when contains value with the same key.
     * <p>
     * function arguments are 1st is saved value and 2nd is target value.
     *
     * @param conflictMessageProducer
     *          A function for producing a conflict error message
     *
     * @return a conflict error message
     * 指定一个函数，用于在包含具有相同键的值时产生冲突错误消息。
     * @since 3.5.0
     */
    public StrictMap<V> conflictMessageProducer(BiFunction<V, V, String> conflictMessageProducer) {
      this.conflictMessageProducer = conflictMessageProducer;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(String key, V value) {
      if (containsKey(key)) {
        throw new IllegalArgumentException(name + " already contains key " + key
            + (conflictMessageProducer == null ? "" : conflictMessageProducer.apply(super.get(key), value)));
      }
      if (key.contains(".")) { // 如果存在.的话，可能是全路径名称 com.wf.demo.X
        final String shortKey = getShortName(key); //得到一个较短的名称
        if (super.get(shortKey) == null) {
          super.put(shortKey, value);
        } else {
          super.put(shortKey, (V) new Ambiguity(shortKey));// 如果已经存在此短名的话，包装成有歧义的对象进行存储
        }
      }
      return super.put(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
      if (key == null) {
        return false;
      }

      return super.get(key) != null;
    }

    @Override
    public V get(Object key) {
      V value = super.get(key);
      if (value == null) {
        throw new IllegalArgumentException(name + " does not contain value for " + key);
      }
      if (value instanceof Ambiguity) {
        throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
            + " (try using the full name including the namespace, or rename one of the entries)");
      }
      return value;
    }

    protected static class Ambiguity {
      private final String subject;

      public Ambiguity(String subject) {
        this.subject = subject;
      }

      public String getSubject() {
        return subject;
      }
    }

    private String getShortName(String key) {
      final String[] keyParts = key.split("\\.");
      return keyParts[keyParts.length - 1];
    }
  }

}
