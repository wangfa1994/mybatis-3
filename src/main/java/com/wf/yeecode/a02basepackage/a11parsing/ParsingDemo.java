package com.wf.yeecode.a02basepackage.a11parsing;

import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ParsingDemo {


  public static void main(String[] args) throws Exception {
    // genericTokenParser();

    //propertyParser();
    xPathParser();
  }


  private static void xPathParser() throws Exception {
    InputStream resourceAsStream = Resources.getResourceAsStream("sourceCode/a01/sqlMapConfig.xml");
    XPathParser xPathParser = new XPathParser(resourceAsStream, true, null, new XMLMapperEntityResolver());
    XNode xNode = xPathParser.evalNode("/configuration");
    System.out.println(xNode);
  }

  private static void propertyParser(){
    Properties properties = new Properties();properties.put("id","1");properties.put("name","zhangSan");
    String parse = PropertyParser.parse("this student id is ${id},name is ${name},over!", properties);
    System.out.println(parse);


  }

  private static void genericTokenParser() {
    Map<String,String> variables = new HashMap<>();
    variables.put("id","1");
    variables.put("cat","tom");

    GenericTokenParser genericTokenParser = new GenericTokenParser("#{","}",new VariableTokenHandler(variables));
    String parse = genericTokenParser.parse("select * from table_name where id = #{id}");
    System.out.println(parse);

    GenericTokenParser genericTokenParser1 = new GenericTokenParser("${","}",new VariableTokenHandler(variables));
    String parse1 = genericTokenParser1.parse("${cat} say id is ${id},is me");
    System.out.println(parse1);
  }


  public static class VariableTokenHandler implements TokenHandler {
    private Map<String, String> variables = new HashMap<>();

    VariableTokenHandler(Map<String, String> variables) {
      this.variables = variables;
    }

    @Override
    public String handleToken(String content) {
      return variables.get(content);
    }
  }

}
