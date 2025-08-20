package com.wf.yeecode.a04corepackage.a24plugin.custPlugin;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/* 自定义插件 插件的功能是：在 MyBatis查询列表形式的结果时，打印出结果的数目
* */
@Intercepts({ @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}) })
public class MyPluginResultSetHandler implements Interceptor {

  private String info;


  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 执行原有的方法
    Object result = invocation.proceed();
    // 打印原方法输出结果的数目
    System.out.println(info+"："+ ((List)result).size());
    return result; // 返回原定结果
  }

  @Override
  public Object plugin(Object target) {
    return Interceptor.super.plugin(target);
  }

  @Override // 为自定义拦截器设置属性
  public void setProperties(Properties properties) {

    info = properties.get("preInfo").toString();

  }
}
