package com.wf.yeecode.a04corepackage.a19cache;

public class CacheMain {

  /***
   * MyBatis 缓存使得每次数据库查询请求都会先经过缓存系统的过滤，只有在没有命中缓存的情况下才会去查询物理数据库
   * cache包就是 MyBatis缓存能力的提供者
   * cache包只是提供了缓存能力，不涉及具体缓存功能的使用
   *
   * 1. Java对象的引用级别
   * java中的引用级别不只有【引用】【不引用】两种情况。而是【强软弱虚】四种情况
   *
   *
   * 2. Cache顶级接口
   * cache包是典型的装饰器模式应用案例,在impl子包中存放了实现类，在decorators包中存放的是装饰器类
   *
   * 3.缓存键
   * mybatis的缓存键，必须 无碰撞，高效比较 高效生成
   *【使用数值、字符串等简单类型作为键，这类键容易产生碰撞。为了防止碰撞的发生，需要将键的生成机制设计得非常复杂，这又降低了键的比较效率和生成效率】
   *
   * 设计了一个 CacheKey类作为缓存键 ，使用了一些属性进行组成我们的缓存键
   *
   * 在数据库查询时会先根据当前的查询条件生成一个 CacheKey, BaseExecutor#createCacheKey(MappedStatement,Object,RowBounds,BoundSql)
   *
   * 4.缓存的实现
   * 实现类是 PerpetualCache 一个缓存容器，一个缓存id,缓存实现类 PerpetualCache的实现非常简单，但可以通过装饰器来为其增加更多的功能
   * 5. 缓存装饰器
   * 同步装饰器：为缓存增加同步功能 SynchronizedCache
   * 日志装饰器  为缓存增加日志功能， LoggingCache
   * 清理装饰器： 为缓存中的数据增加清理功能 FifoCache  LruCache  WeakCache 、SoftCache
   * 阻塞装饰器： 为缓存增加阻塞功能， BlockingCache。
   * 定时清理装饰器 为缓存增加定时刷新功能， ScheduledCache
   * 序列化装饰者： 为缓存增加序列化功能， SerializedCache
   * 事务装饰器：用于支持事务操作的装饰器 TransactionalCache
   *
   * 6.缓存的组建
   *  组建缓存的过程就是根据需求为缓存的基本实现增加各种装饰的过程。这个过程是在CacheBuilder中完成的。
   *
   * 7.事务的缓存
   *  事务产生的数据需要再事务提交的时候才能放入到缓存，如果再事务期间写入缓存，可能会出现脏数据
   *  TransactionalCache装饰器
   *  在一个事务中，可能会涉及多个缓存。 TransactionalCacheManager 就是用来管理一个事务中的多个缓存的
   *
   *
   * 8. mybatis的缓存机制
   * 缓存机制是在 Executor 接口 中进行处理的，
   * Executor 接口是执行器接口，它负责进行数据库查询等操作 存在两个直接子类  CachingExecutor类和 BaseExecutor类
   *  CachingExecutor是一个装饰器类，能够为执行器实现类增加缓存功能
   *  BaseExecutor类是所有实际执行器类的基类， 存在四个子类：SimpleExecutor ，  BatchExecutor ，  ReuseExecutor ，ClosedExecutor
   *
   *
   * 一级缓存：  又叫本地缓存  与它相关的配置项有两个
   *  配置文件中： <setting name="localCacheScope" value="SESSION"/>  session 一次会话， Statement一条语句 默认session
   *
   *  映射文件中：数据库操作节点内增加 flushCache属性项 默认为false
   *      <select id="findById" resultType="User" parameterType="java.lang.Integer" flushCache="true">
   *        当设置为true时，MyBatis会在该数据库操作执行前清空一、二级缓存
   *         select * from user where id = #{id}
   *     </select>
   *
   * 一级缓存功能由 BaseExecutor类实现，BaseExecutor类作为实际执行器的基类，为所有实际执行器提供一些通用的基本功能，
   * 在这里增加缓存也就意味着每个实际执行器都具有这一级缓存。 query方法主要处理新增缓存，update方法中主要处理更新缓存
   * 一级缓存就是 BaseExecutor中的两个 PerpetualCache类型的属性，其作用范围很有限，不支持各种装饰器的修饰，因此不能进行容量配置、清理策略设置及阻塞设置
   *
   *
   *
   * 二级缓存 ：二级缓存的作用范围是一个命名空间（即一个映射文件），而且可以实现多个命名空间共享一个缓存。因此与一级缓存相比其作用范围更广，且选择更为灵活
   * 与二级缓存有关的配置有四个：
   *    1. 配置文件的 settings节点下  <setting name="cacheEnabled" value="true"/> 默认就是开启的
   *    2. 映射文件的<cache/> 和 <cache-ref namespace="user"/>  标签 开启并配置本命名空间的缓存设置引用别的命名空间下的缓存 不配置的话表示此命名空间没有缓存，这两个标签只有在开启配置文件中标签的话才生效
   *    3. 映射文件中的数据库操作节点内的 useCache属性，通过它可以配置该数据库操作节点是否使用二级缓存。只有当第一、二项配置均启用了缓存时，该项配置才有效。对于 SELECT类型的语句而言，useCache属性的默认值为 true
   *    4. 映射文件中的数据库操作节点内的 flushCache属性
   *
   * 二级缓存功能由 CachingExecutor 进行实现处理，它是一个装饰器类，能通过装饰实际执行器为它们增加二级缓存功能
   * Configuration.newExecutor用于创建Executor,进行装饰创建
   *
   * 一级缓存由 BaseExecutor 通过两个PerpetualCache类型的属性提供，而二级缓存由CachingExecutor包装类提供。
   * CachingExecutor作为装饰器会先运行，然后才会调用实际执行器，这时 BaseExecutor 中的方法才会执行。因此，在数据库查询操作中，MyBatis 会先访问二级缓存再访问一级缓存
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   */
}
