/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.type;

/**
 * @author Clinton Begin
 */
class ByteArrayUtils {

  private ByteArrayUtils() {
    // Prevent Instantiation
  }

  static byte[] convertToPrimitiveArray(Byte[] objects) { // 转换为原始数组 包装类型到基本类型
    final byte[] bytes = new byte[objects.length];
    for (int i = 0; i < objects.length; i++) {
      bytes[i] = objects[i];
    }
    return bytes;
  }

  static Byte[] convertToObjectArray(byte[] bytes) {
    final Byte[] objects = new Byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      objects[i] = bytes[i];
    }
    return objects;
  }
}
