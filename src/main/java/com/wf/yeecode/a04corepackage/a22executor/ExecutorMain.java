package com.wf.yeecode.a04corepackage.a22executor;

public class ExecutorMain {

  /**
   * 一个类必须通过类加载过程将类文件加载到 JVM后才能使用。另外我们也能够直接修改 JVM中的字节码信息来修改和创建类，
   * cglib就是基于这个原理工作的。cglib使用字节码处理框架 ASM来转换字节码并生成被代理类的子类，然后这个子类就可以
   * 作为代理类展开工作。ASM是一个底层的框架，除非你对 JVM内部结构包括 class文件的格式和指令集都很熟悉，否则不要直接使用
   * cglib基于底层的 ASM框架来实现 Java字节码的修改
   * javassist和 ASM类似，它也是一个开源的用来创建、修改 Java字节码的类库，能实现类的创建、方法的修改、继承关系的设置等一系列的操作
   * 相比于 ASM，javassist的优势是学习成本低，可以根据 Java代码生成字节码，而不需要直接操作字节码
   *
   * javassist 这个强大的工具是可以直接修改字节码的。因此，我们可以使用它创建被代理类的子类从而实现动态代理，也可以使用它创建被代理类接口的子类从而实现动态代理。
   *
   *
   * 序列化和反序列化
   * Serializable接口或者 Externalizable 接口
   * Externalizable接口存在两个方法，我们可以更灵活的自定义我们自己的
   * writeExternal（ObjectOutput out）：该方法在目标对象序列化时调用。方法中可以调用 DataOutput（输入参数 ObjectOutput的父类）方法来保存其基本值，
   *                                    或调用ObjectOutput的 writeObject方法来保存对象、字符串和数组
   * readExternal（ObjectInput in）: 该方法在目标对象反序列化时调用。方法中调用DataInput（输入参数 ObjectInput的父类）方法来恢复其基础类型，
   *                                或调用 readObject方法来恢复对象、字符串和数组。
   *
   * 需要注意的是，readExternal 方法读取数据时，必须与writeExternal方法写入数据时的顺序和类型一致
   *
   * 序列化中和反序列化中两个神奇的方法：writeReplace方法和 readResolve方法
   * writeReplace：如果一个类中定义了该方法，则对该类的对象进行序列化操作前会先调用该方法，最终该方法返回的对象将被序列化。
   * readResolve: 如果一个类中定义了该方法，则对该类的对象进行反序列化操作后会调用该方法，最终该方法返回的对象将作为反序列化的结果
   *
   * 原始对象----> writeReplace方法 ----> writeExternal方法 ----> 序列化结果 ----> readExternal方法 ----> readResolve方法  ----->反序列化得到对象
   *
   *
   * ThreadLocal 是典型的“空间换时间”思路的应用，每个线程都独有一个ThreadLocal，可以在其中存放独属于该线程的数据.
   *
   * 存储过程：存储过程是数据库中的一段可以被重用的代码片段，可以通过外部调用完成较为复杂的操作。
   * 在调用时，可以为存储过程传入输入参数，而存储过程执行结束后也可以给出输出参数。
   *
   * Statement及其子接口
   *
   * Statement接口中定义了一些抽象方法能用来执行静态SQL语句并返回结果，通常返回的结果是一个结果集ResultSet
   * PreparedStatement接口 继承了Statement接口的方法功能，并且进行扩展实现了可以预编译的语句执行
   *  新增的set方法使得预编译的SQL语句具有了按照参数位置对参数赋值的功能
   * CallableStatement接口 又继承了PreparedStatement接口的方法功能，并且进行扩展实现了支持存储过程的语句功能
   *  进一步增加四类方法：
   * 照参数名称赋值方法：这一类方法能够为存储过程中指定名称的参数赋值。例如，setInt（String，int）方法就属于这一类
   * 注册输出参数方法：这一类方法能够向存储过程注册输出参数。例如，registerOutParameter（int，int）方法就属于这一类
   * 按照参数位置读取值方法：这一类方法能够读取存储过程指定位置的参数值。例如，getInt（int）方法就属于这一类方法。
   * 按照参数名称读取值方法：这一类方法能够读取存储过程指定名称的参数的值。例如，getInt（String）方法就属于这一类方法。
   *
   * 从 Statement接口到 PreparedStatement接口再到 CallableStatement接口，功能越来越强大。这就意味着SQL语句中，从简单
   * 语句到预编译语句再到存储过程语句，它们支持的功能越来越多
   * Statement对象的getGeneratedKeys方法返回此语句操作自增生成的主键，没有产生产生自增主键，返回空的ResultSet对象
   *
   *
   * 主键自增功能 KeyGenerator
   * 配置文件中进行设置自增主键功能
   * MyBatis的 executor包中的 keygen子包兼容两类情况，
   * 一类情况是数据库支持主键自增功能的数据库，MySQL数据库、SQL Server数据库等
   * 一类情况是数据库不支持主键自增功能的数据库，Oracle数据库
   * KeyGenerator接口，只能有一种实现类生效。
   *  Jdbc3KeyGenerator：
   *  SelectKeyGenerator：
   *  NoKeyGenerator：
   *  启用 Jdbc3KeyGenerator进行设置：
   *  <setting name="userGeneratedKey" value="true"/>
   *  或者直接在映射文件的相关语句上启用useGeneratedKeys
   *
   *  启用 SelectKeyGenerator进行设置
   *  在映射文件中的相关语句加上selectKey标签
   *
   *  如果某一条语句设置了useGeneratedKeys 和 selectKey 则后者生效
   *
   *
   * Jdbc3KeyGenerator类是为具有主键自增功能的数据库准备的，他的作用不是产生自增主键，而是提供自增主键的回写功能。
   * Jdbc3KeyGenerator类提供的回写功能能够将数据库中产生的 id值回写给 Java对象本身，数据库主键自增结束后，
   * 将自增出来的主键读取出来并赋给 Java 对象。这些工作都是在数据插入完成后进行的，即在 processAfter 方法中进行
   *
   * SelectKeyGenerator类则是真正的实现了自增主键，适合没有自增主键功能的数据库的使用
   *
   *
   * 懒加载功能
   * 先加载必需的信息，然后再根据需要进一步加载信息的方式叫作懒加载
   * <setting name="lazyLoadingEnabled" value="true"/> 通用的懒加载设置
   *
   * <setting name="aggressiveLazyLoading" value="true"/> 激进的懒加载设置
   * 当aggressiveLazyLoading设置为true时，对对象【任一属性的读或写操作】都会触发该对象所有懒加载属性的加载
   * 当aggressiveLazyLoading设置为false时，对对象【某一懒加载属性的读操作】会触发该属性的加载。
   * 不论aggressiveLazyLoading设置如何，调用对象的equals，clone，hashCode，toString中任意一个方法都会触发该对象所有懒加载属性的加载。
   *
   * 基于loader子包的懒加载实现
   *
   * ProxyFactory是创建代理类的工厂接口 实现类【CglibProxyFactory类 和 JavassistProxyFactory类】
   *
   * ResultLoaderMap类  代理对象可能会有多个属性可以被懒加载，这些尚未完成加载的属性是在ResultLoaderMap 类的实例中存储的
   *
   * 【内部类ResultLoaderMap.LoadPair#load方法，懒加载的过程就是执行懒加载 SQL语句后，将查询结果使用输出结果加载器赋给输出结果元对象的过程。load方法
   * 首先会判断输出结果元对象metaResultObject和输出结果加载器resultLoader是否存在，如果不存在的话，就会使用输入参数userObject重新创建这两个对象】
   *
   * 【ClosedExecutor类的设计，是ResultLoaderMap的内部类，该类只有一个 isClosed方法能正常工作，在load方法中创建ResultLoader被使用，用来表明自己是一个被关闭的类，
   * 可以让任何遇到CloseExecutor对象的操作都会重新创建一个新的有实际功能的Executor】
   *
   * ResultLoader类 一个结果加载器类，它负责完成数据的加载工作。因为懒加载只涉及查询，而不需要支持增、删、改的工作，因此它只有一个查询方法 selectList来进行数据的查询
   *
   *
   *
   * 序列化和反序列化功能
   *
   *
   *
   *
   *
   *
   * 语句处理功能
   * 在映射文件的编写中：
   *   ${}：使用这种符号的变量将会以字符串的形式直接插到 SQL片段中。
   *   ＃{}：使用这种符号的变量将会以预编译的形式赋值到 SQL片段中。
   *
   *  MyBatis中支持三种语句类型 ,不同语句类型支持的变量符号不同
   *
   *  STATEMENT语句类型：这种语句类型中，只会对 SQL片段进行简单的字符串拼接。因此，只支持使用${}定义变量
   *  PREPARED语句类型：这种语句类型中，会先对SQL片段进行字符串拼接，然后对 SQL片段进行赋值。因此，支持使用${}＃{}这两种形式定义变量
   *  CALLABLE：这种语句类型用来实现执行过程的调用，会先对 SQL 片段进行字符串拼接，然后对 SQL片段进行赋值。因此，支持使用${}＃{}这两种形式定义变量
   *  在创建SQL时，语句的类型由 statementType属性进行指定。不指定则默认采用 PREPARED类型。
   *
   *  statement子包负责提供语句处理功能 ，StatementHandler是语句处理功能类的父接口,用于处理Statement
   *  SimpleStatementHandler中 parameterize方法的实现为空，因为它只需完成字符串替换即可，不需要进行参数处理
   *  PreparedStatementHandler中 parameterize方法最终通过 ParameterHandler接口经过多级中转后调用了 java.sql.PreparedStatement类中的参数赋值方法
   *  CallableStatementHandler中 parameterize方法完成两步工作：一是通过 registerOutputParameters方法中转后调用 java.sql.CallableStatement中的输出参数注册方法完成输出参数的注册；
   *  二是通过 ParameterHandler接口经过多级中转后调用 java.sql.PreparedStatement类中的参数赋值方法
   *  三个实现都是依赖java.sql包下的 Statement接口及其子接口提供的功能完成具体参数处理操作的
   *
   *
   *参数处理功能
   *  为 SQL语句中的参数赋值是 MyBatis进行语句处理时非常重要的一步，而这一步就是由 parameter子包完成的
   *  ParameterHandler接口 只有一个默认的实现类  DefaultParameterHandler
   *  getParameterObject方法用来获取 SQL语句对应的实参对象
   *  getParameterObject方法用来获取 SQL语句对应的实参对象
   *
   *
   *
   *结果处理功能
   * MyBatis查询结果的处理，需要完成的功能有
   *  1. 处理结果映射中的嵌套映射等逻辑
   *  2. 根据映射关系，生成结果对象
   *  3. 根据数据库查询记录对结果对象的属性进行赋值
   *  4. 将结果对象汇总为 List、Map、Cursor等形式
   *
   *  子包result只负责完成 将结果对象汇总为 List、Map、Cursor等形式，Cursor包实现cursor
   *
   * session包中的
   * ResultContext接口(结果上下文，其中存放了数据库操作的一个结果，对应数据库中的一条记录)
   * ResultHandler接口(结果处理器，数据库操作结果会由它进行处理) 会进行处理ResultContext
   *
   * 回到result子包
   * DefaultResultContext类【ResultContext唯一实现,对应数据库中的一条记录】
   * DefaultResultHandler类，DefaultMapResultHandler类【ResultHandler实现类】
   *
   * 通过ResultHandler将我们的数据库结果ResultContext处理成我们的List,Map,Cursor形式的对象返回
   * DefaultResultHandler类负责将多个 ResultContext聚合为一个 List返回。
   * DefaultMapResultHandler类负责将多个 ResultContext聚合为一个 Map返回。
   * DefaultCursor 类中的 ObjectWrapperResultHandler 内部类负责将多个 ResultContext聚合为一个 Cursor返回
   *
   *
   * 结果集处理功能
   * 通过result子包，将数据库单个结果对象聚合为 List、Map、Cursor，
   * 通过resultset子包：处理结果映射中的嵌套映射等逻辑，根据映射关系，生成结果对象，根据数据库查询记录对结果对象的属性进行赋值
   *
   * ResultSetWrapper是结果封装类，
   * ResultSetHandler是结果集处理器的接口
   * DefaultResultSetHandler是实现类
   *
   * 结果：从数据库中查询出的一条记录就是一个结果，它可以映射为一个 Java对象
   * 结果集：指结果的集合，从数据库中查询出的多个记录就是一个结果集。结果集可以以 List、Map、Cursor的形式返回
   * 多结果集：即结果集的集合，其中包含了多个结果集【union操作】
   *
   * java.sql.Statement进行完数据库操作之后，对应的操作结果是由 java.sql.ResultSet返回的
   * ResultSet接口的方法被分为四类
   *  1：切换到下一结果，读取本结果是否为第一个结果，最后一个结果等结果间切换相关的方法
   *  2：读取当前结果某列的值
   *  3：修改当前结果某列的值（修改不会影响数据库中的真实值）
   *  4：些其他的辅助功能，如读取所有列的类型信息等。
   *
   *
   *
   * 执行器
   * executor包中的各个子包为执行器提供一些子功能。最终这些子功能都是有Executor接口以及企实现类串接起来，共同向外提供服务
   * CachingExecutor 是一个特殊的执行器，没有包括任何具体的数据库操作，在其他数据库操作的基础上封装了一层缓存
   *
   * 执行器基类BaseExecutor。是一个抽象类，并用到了模板模式。它实现了其子类的一些共有的基础功能，而将与子类直接相关的操作交给子类处处理。
   * BaseExecutor进行定义了一些方法，让子类去进行实现
   * 子类主要包括四个实现类：
   * ClosedExecutor: 一个仅能表示自己已经关闭的执行器，没有其他实际功能
   * SimpleExecutor:一个最为简单的执行器
   * BatchExecutor: 支持批量执行功能的执行器
   * ReuseExecutor: 支持Statement对象复用的执行器
   * SimpleExecutor、BatchExecutor、ReuseExecutor 这三个执行器的选择是在MyBatis的配置文件中进行的,，可选的值由 session包中的枚举类 ExecutorType定义
   * 这三个执行器主要基于 StatementHandler完成创建 Statement对象、绑定参数等工作
   *
   *
   *
   * 错误上下文
   *很多方法的开始阶段都会调用 ErrorContext 类的相关方法
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
