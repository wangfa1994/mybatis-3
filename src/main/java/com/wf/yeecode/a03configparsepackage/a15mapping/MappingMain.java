package com.wf.yeecode.a03configparsepackage.a15mapping;

public class MappingMain {

  /**
   *  1. SQL语句处理功能
   * MappedStatement类：表示数据库的操作节点的内容，select inset update delete四个节点内的所有内容
   * SqlSource接口：数据库操作标签中包含的sql语句 四个实现类 providerSqlSource DynamicSqlSource RawSqlSource StaticSqlSource
   * BoundSql类：sqlSource进一步处理的产物
   *
   * MappedStatement
   * <insert id="addUser" parameterType="com.wf.a28.model.User">
   *         INSERT INTO `user`
   *         (`name`,`email`,`age`,`sex`,`schoolName`)
   *         VALUES
   *         (#{name},#{email},#{age},#{sex},#{schoolName})
   * </insert>
   *
   *
   * SqlSource
   *         INSERT INTO `user`
   *         (`name`,`email`,`age`,`sex`,`schoolName`)
   *         VALUES
   *         (#{name},#{email},#{age},#{sex},#{schoolName})
   *
   *
   *
   * BoundSql
   *         INSERT INTO `user`
   *         (`name`,`email`,`age`,`sex`,`schoolName`)
   *         VALUES
   *         (tom,email@qq.com,12,1,'清北')
   *
   *
   *
   * 2. 输出结果的处理功能
   * 在映射文件的数据库操作节点中，可以直接使用 resultType设置将输出结果映射为 Java对象。
   * 使用 resultMap来定义输出结果的映射方式，更为灵活和强大的方式 【】
   *
   * resultMap 解析实体类  一个resultMap标签就是一个实体类
   * resultMapping 解析实体类  resultMapping对应一条条的子标签id,name
   * Discriminator 解析实体类
   *
   *      根据结果中的sex属性的不同输出不同的子类
   *     <resultMap id="userMap" type="User" autoMapping="false">
   *         <id property="id" column="id" javaType="Integer" jdbcType="INTEGER"
   *             typeHandler="org.apache.ibatis.type.IntegerTypeHandler"/>
   *         <result property="name" column="name"/>
   *         <discriminator javaType="int" column="sex">
   *             <case value="0" resultMap="boyUserMap"/>
   *             <case value="1" resultMap="girlUserMap"/>
   *         </discriminator>
   *     </resultMap>
   *     <resultMap id="girlUserMap" type="Girl" extends="userMap">
   *         <result property="email" column="email"/>
   *     </resultMap>
   *     <resultMap id="boyUserMap" type="Boy" extends="userMap">
   *         <result property="age" column="age"/>
   *     </resultMap>
   *
   *
   * 3. 输入参数处理功能
   *  MyBatis不仅可以将数据库结果映射为对象，还能够将对象映射成SQL语句需要的输入参数。
   *  ParameterMap、ParameterMapping这两个类，它们也都是解析实体类 【parameterMap标签是老式风格的参数映射，未来可能会废弃。更好的办法是使用内联参数】
   *
   *
   *     <parameterMap id="userParam" type="User">
   *         <parameter property="name" javaType="String"/>      ParameterMapping
   *         <parameter property="schoolName" javaType="String"/>
   *     </parameterMap>
   *
   *
   * 4. 多数据库种类处理功能
   * MyBatis 支持多种数据库 ，不同类型的数据库之间支持的 SQL 规范略有不同。
   * 同样是限制查询结果的条数，在 SQL Server中要使用 TOP关键字，而在 MySQL中要使用 LIMIT 关键字
   * 为了能够兼容不同数据库的 SQL规范 在使用多种数据库前，需要先在配置文件中列举要使用的数据库类型,然后在sql语句上再标识其对应的数据库类型
   *
   * 多数据支持的实现由 DatabaseIdProvider接口负责
   *
   *     <databaseIdProvider type="DB_VENDOR">
   *         <property name="MySQL" value="mysql" />
   *         <property name="SQL Server" value="sqlserver" />
   *     </databaseIdProvider>
   *
   * <select id="selectByAge" resultMap="userMap" databaseId="mysql">
   *         SELECT * FROM `user` WHERE `age` = #{age} TOP 5
   *     </select>
   *
   *     <select id="selectByAge" resultMap="userMap" databaseId="sqlserver">
   *         SELECT * FROM `user` WHERE `age` = #{age} LIMIT 5
   *     </select>
   *
   *
  *
  * */
}
