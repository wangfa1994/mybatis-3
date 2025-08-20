package com.wf.yeecode.a04corepackage.a18jdbc;

import org.apache.ibatis.jdbc.AbstractSQL;
import org.apache.ibatis.jdbc.Null;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.jdbc.SqlRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JdbcDemo {

  public static void main(String[] args) throws Exception {
    sqlRunner();

    //customerSql();
    // sql();
  }

  private static void sqlRunner() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/lagou?useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true",
      "root", "root");

    SqlRunner sqlRunner = new SqlRunner(connection);

    Map<String, Object> nullOne = sqlRunner.selectOne("select * from user where id = ? and 1=?", Null.INTEGER,1);
    System.out.println(nullOne);

    List<Map<String, Object>> rows = sqlRunner.selectAll("SELECT * FROM user");
    System.out.println(rows);

    Map<String, Object> selectOne = sqlRunner.selectOne("select * from user where id = 1");
    System.out.println(selectOne);
  }

  private static void customerSql() {
    ExplainSQL sql = new ExplainSQL();
    ExplainSQL from = sql.SELECT("a").FROM("Account a");
    System.out.println(from.toString());
  }

  // 自定义扩展我们的sql语句功能
  static class ExplainSQL extends AbstractSQL<ExplainSQL>{
    @Override
    public ExplainSQL getSelf() {
      return this;
    }

    @Override
    public String toString() {
      String string = super.toString();
      return "explain "+string;
    }
  }

  private static void sql() {
    SQL sql = new SQL();
    sql.SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME")
        .SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON")
          .FROM("PERSON P")
            .FROM("ACCOUNT A")
              .INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID")
                .INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID")
                  .WHERE("P.ID = A.ID")
                    .WHERE("P.FIRST_NAME like ?")
                      .OR()
                        .WHERE("P.LAST_NAME like ?")
                          .GROUP_BY("P.ID")
                            .HAVING("P.LAST_NAME like ?")
                              .OR()
                                .HAVING("P.FIRST_NAME like ?")
                                  .ORDER_BY("P.ID")
                                    .ORDER_BY("P.FULL_NAME");
    String string = sql.toString();
    System.out.println("拼接的："+string);
    /* 实例代码块的写法*/
    SQL sql1 = new SQL() {
       {
        SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
        SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
        FROM("PERSON P");
        FROM("ACCOUNT A");
        INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
        INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
        WHERE("P.ID = A.ID");
        WHERE("P.FIRST_NAME like ?");
        OR();
        WHERE("P.LAST_NAME like ?");
        GROUP_BY("P.ID");
        HAVING("P.LAST_NAME like ?");
        OR();
        HAVING("P.FIRST_NAME like ?");
        ORDER_BY("P.ID");
        ORDER_BY("P.FULL_NAME");
      }
    };
    System.out.println("代码块："+string.toString());
  }
}
