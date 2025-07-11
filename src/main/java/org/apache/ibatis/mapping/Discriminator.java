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
package org.apache.ibatis.mapping;

import java.util.Collections;
import java.util.Map;

import org.apache.ibatis.session.Configuration;

/** resultMap内部的鉴别器，就像程序中的选择语句一样，它使得数据查询结果能够根据某些条件的不同而进行不同的映射
 * @author Clinton Begin
 * <discriminator javaType="int" column="sex"><case value="0" resultMap="boyUserMap"/><case value="1" resultMap="girlUserMap"/></discriminator>
 */
public class Discriminator {

  private ResultMapping resultMapping; // 存储条件判断行的条件，注意也是一个resultMapping
  private Map<String, String> discriminatorMap; // 存储选择项的信息，键为value,值为resultMap值

  Discriminator() {
  }

  public static class Builder { // 内部类的建造者模式
    private final Discriminator discriminator = new Discriminator();

    public Builder(Configuration configuration, ResultMapping resultMapping, Map<String, String> discriminatorMap) {
      discriminator.resultMapping = resultMapping;
      discriminator.discriminatorMap = discriminatorMap;
    }

    public Discriminator build() {
      assert discriminator.resultMapping != null;
      assert discriminator.discriminatorMap != null;
      assert !discriminator.discriminatorMap.isEmpty();
      // lock down map
      discriminator.discriminatorMap = Collections.unmodifiableMap(discriminator.discriminatorMap);
      return discriminator;
    }
  }

  public ResultMapping getResultMapping() {
    return resultMapping;
  }

  public Map<String, String> getDiscriminatorMap() {
    return discriminatorMap;
  }

  public String getMapIdFor(String s) {
    return discriminatorMap.get(s);
  }

}
