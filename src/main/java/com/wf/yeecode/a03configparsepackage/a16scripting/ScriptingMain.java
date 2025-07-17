package com.wf.yeecode.a03configparsepackage.a16scripting;

public class ScriptingMain {


  /**
   * 1. OGNL表达式
   * OGNL（Object Graph Navigation Language，对象图导航语言） 是一种功能强大的表达式语言（Expression Language，EL）
   * 通过它，能够完成从集合中选取对象、读写对象的属性、调用对象和类的方法、表达式求值与判断等操作
   * JSP、XML中常常见到 OGNL表达式,OGNL 是一种广泛、便捷、强大的语言
   *
   * 表达式（expression）：是一个带有语法含义的字符串，是整个 OGNL的核心内容。通过表达式来确定需要进行的 OGNL操作
   * 根对象（root）：可以理解为 OGNL 的被操作对象。表达式中表示的操作就是针对这个对象展开的
   * 上下文（context）：整个 OGNL处理时的上下文环境，该环境是一个 Map对象。在进行 OGNL处理之前，我们可以传入一个初始化过的上下文环境
   *
   * 2. LanguageDriver为语言驱动类的接口
   *   LanguageDriverRegistry
   *   LanguageDriver
   *
   * 3. Sql节点数的组建
   *
   *  映射文件中的数据库操作语句，实际上是由众多SQL节点组成的一棵树 要想解析这棵树，首先要做的是将 XML中的信息读取进来，
   *  然后在内存中将 XML树组建为 SQL 节点树。SQL 节点树的组建由 XMLScriptBuilder 类负责
   *  内嵌了一个接口标准NodeHandler，将我们的结点拼接到节点树上，不同的标签有不同的处理器，
   *
   *      <select id="selectUsersByNameOrSchoolName" parameterMap="userParam01" resultType="User">
   *         SELECT * FROM `user`
   *         <where>
   *             <if test="name != null">
   *                 `name` = #{name}
   *             </if>
   *             <if test="schoolName != null">
   *                 AND `schoolName` = #{schoolName}
   *             </if>
   *         </where>
   *      </select>
   *
   * 4. SQL节点树的解析
   *  对组建好的 SQL 节点树进行解析是 MyBatis 中非常重要的工作。这部分工作主要在scripting包的 xmltags子包中展开
   *    1. 针对OGNL的处理 完成 OGNL 的解析工作，xmltags 子包中还设置了三个相关的类，它们分别是 OgnlClassResolver类、OgnlMemberAccess类、OgnlCache类
   *       OgnlClassResolver类 继承了ONGL中的类，覆盖方法，用Mybatis的形式去返回一个类名称的Class对象
   *       OgnlMemberAccess类 封装属性的可访问性，主要通过反射机制进行处理
   *       OgnlCache类  为了提升 OGNL 的运行效率，MyBatis 还为 OGNL 提供了一个缓存，即 OgnlCache类
   *
   *    2. ExpressionEvaluator表达式求值器： MyBatis并没有将 OGNL工具直接暴露给各个 SQL节点使用，而是对 OGNL工具进行了进一步的易用性封装
   *
   *    3. DynamicContext： 在进行 SQL节点树的解析时，需要不断保存已经解析完成的 SQL片段。另外，在进行SQL节点树的解析时也需要一些参数和环境信息作为解析的依据
   *      这两个功能是由动态上下文 DynamicContext提供的
   *
   *    4. SqlNode:SqlNode是一个接口，接口中只定义了一个 apply方法。该方法负责完成节点自身的解析，并将解析结果合并到输入参数提供的上下文环境中
   *         <select id="selectUsersByNameOrSchoolName" parameterMap="userParam01" resultType="User">
   *               SELECT * FROM `user`
   *              <where>                            whereSqlNode
   *                  <if test="name != null">       ifSqlNode
   *                      `name` = #{name}
   *                  </if>
   *                  <if test="schoolName != null">
   *                     AND `schoolName` = #{schoolName}
   *                 </if>
   *             </where>
   *          </select>
   *
   *
   *
   * 5.SqlSource复盘
   * 语言驱动类完成的主要工作就是生成SqlSource，在语言驱动接口LanguageDriver的三个方法中，有两个方法是用来生成 SqlSource
   * 的。而 SqlSource 子类的转化工作也主要在scripting包中完成
   *
   * DynamicSqlSource：动态 SQL语句。所谓动态 SQL语句是指含有动态 SQL节点（如if节点）或者含有“${}”占位符的语句
   * RawSqlSource：原生 SQL语句。指非动态语句，语句中可能含有“＃{}”占位符，但不含有动态 SQL节点，也不含有“${}”占位符
   * StaticSqlSource：静态语句。语句中可能含有“?”，可以直接提交给数据库执行
   * ProviderSqlSource：上面的几种都是通过 XML 文件获取的 SQL 语句，而ProviderSqlSource是通过注解映射的形式获取的 SQL语句
   *
   * SqlSource的xml模式生成是通过LanguageDriver[XMLLanguageDriver类]进行委派XMLScriptBuilder给产生的
   * 解析注解信息生成的 SqlSource 对象是 ProviderSqlSource 对象；然后，ProviderSqlSource对象在getBoundSql的时候通过 LanguageDriver接口中的 createSqlSource方法转化为了 DynamicSqlSource对象或者 RawSqlSource对象
   *
   *
   * DynamicSqlSource类在 scripting包的 xmltags子包中，它表示含有动态 SQL节点（如if节点）或者含有“${}”占位符的语句，即动态 SQL语句
   * DynamicSqlSource 和 RawSqlSource都会转化为 StaticSqlSource，然后才能给出一个 BoundSql对象
   *
   * SqlSource 接口有四个实现类，其中三个实现类的对象都通过层层转化变成了StaticSqlSource 对象，然后，SqlSource 接口中定义 的 getBoundSql 抽象方法实际都是由StaticSqlSource对象完成的
   *
   * SqlSource接口的实现类之间的转化过程其实就是数据库操作语句的解析过程。在这个转化过程中，注解中的 SQL语句被分类处理，动态语句被展开，“${}”占位符被赋值，“＃{}”占位符被替换，最终得到了可以交给数据库驱动执行的仅包含参数占位符“？”的SQL语
   * 句
   *
   *
   *
   *
   */
}
