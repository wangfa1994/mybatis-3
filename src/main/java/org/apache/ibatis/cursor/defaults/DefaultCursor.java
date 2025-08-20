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
package org.apache.ibatis.cursor.defaults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/** 这是MyBatis Cursor的默认实现。这个实现不是线程安全的。
 * This is the default implementation of a MyBatis Cursor. This implementation is not thread safe.
 *
 * @author Guillaume Darmont / guillaume@dropinocean.com
 */
public class DefaultCursor<T> implements Cursor<T> {

  // ResultSetHandler stuff
  private final DefaultResultSetHandler resultSetHandler; //结果集处理器
  private final ResultMap resultMap; // 该结果集对应的ResultMap，信息来源于Mapper中的ResultMap节点
  private final ResultSetWrapper rsw; // 返回结果的相信信息
  private final RowBounds rowBounds; //结果的起止信息
  protected final ObjectWrapperResultHandler<T> objectWrapperResultHandler = new ObjectWrapperResultHandler<>(); //内部类，ResultHandler子类，暂存结果的作用

  private final CursorIterator cursorIterator = new CursorIterator(); //内部类，迭代器
  private boolean iteratorRetrieved; // 迭代器存在标志位，迭代器只能给出一次

  private CursorStatus status = CursorStatus.CREATED; // 游标状态
  private int indexWithRowBound = -1; // 记录已经映射的行
  // 游标状态的枚举类
  private enum CursorStatus {

    /** 新创建的游标，数据库ResultSet消费尚未启动。
     * A freshly created cursor, database ResultSet consuming has not started.
     */
    CREATED,
    /** 当前正在使用的游标，数据库ResultSet消费已经开始。
     * A cursor currently in use, database ResultSet consuming has started.
     */
    OPEN,
    /** 一个关闭的游标，没有被完全使用。 结果集没有被完全消费
     * A closed cursor, not fully consumed.
     */
    CLOSED,
    /** 一个完全消耗的游标，消耗的游标总是关闭的。  结果集已经被完全消费
     * A fully consumed cursor, a consumed cursor is always closed.
     */
    CONSUMED
  }

  public DefaultCursor(DefaultResultSetHandler resultSetHandler, ResultMap resultMap, ResultSetWrapper rsw,
      RowBounds rowBounds) {
    this.resultSetHandler = resultSetHandler;
    this.resultMap = resultMap;
    this.rsw = rsw;
    this.rowBounds = rowBounds;
  }

  @Override
  public boolean isOpen() {
    return status == CursorStatus.OPEN;
  }

  @Override
  public boolean isConsumed() {
    return status == CursorStatus.CONSUMED;
  }

  @Override
  public int getCurrentIndex() {
    return rowBounds.getOffset() + cursorIterator.iteratorIndex;
  }

  @Override
  public Iterator<T> iterator() {
    if (iteratorRetrieved) { // 如果迭代器已经给出，抛出异常
      throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
    }
    if (isClosed()) { // 如果游标已经关闭
      throw new IllegalStateException("A Cursor is already closed.");
    }
    iteratorRetrieved = true; // 设置给出迭代器
    return cursorIterator; // 返回迭代器
  }

  @Override
  public void close() {
    if (isClosed()) {
      return;
    }

    ResultSet rs = rsw.getResultSet();
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      // ignore
    } finally {
      status = CursorStatus.CLOSED;
    }
  }

  protected T fetchNextUsingRowBound() { //考虑边界限制条件进行返回数据
    T result = fetchNextObjectFromDatabase(); //在fetchNextObjectFromDatabase的基础上进行考虑边界问题
    while (objectWrapperResultHandler.fetched && indexWithRowBound < rowBounds.getOffset()) { // 如果对象存在单不满足边界限制，则持续读取数据库中的结果中的下一个，直到边界起始位置
      result = fetchNextObjectFromDatabase();
    }
    return result; // 在满足边界限制的情况下，每次从结果集中取出一条结果的功能
  }

  protected T fetchNextObjectFromDatabase() { // 每次调用时都会从数据库查询返回的结果集中取出一条结果。从数据库查询结果中取出下一个对象
    if (isClosed()) {
      return null;
    }

    try {
      objectWrapperResultHandler.fetched = false;
      status = CursorStatus.OPEN;
      if (!rsw.getResultSet().isClosed()) { // 结果集尚未关闭，从结果集中取出一条记录，将其转化为对象，并存入objectWrapperResultHandler中
        resultSetHandler.handleRowValues(rsw, resultMap, objectWrapperResultHandler, RowBounds.DEFAULT, null);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    T next = objectWrapperResultHandler.result; //获得存入objectWrapperResultHandler中的对象
    if (objectWrapperResultHandler.fetched) { // 读取到了新对象
      indexWithRowBound++; // 更改索引，表明记录索引加一
    }
    // No more object or limit reached 没有新对象或者已经到了rowBounds边界
    if (!objectWrapperResultHandler.fetched || getReadItemsCount() == rowBounds.getOffset() + rowBounds.getLimit()) {
      close(); // 游标内的数据已经消费完毕
      status = CursorStatus.CONSUMED;
    }
    objectWrapperResultHandler.result = null; // 清除objectWrapperResultHandler中的result对象，准备迎接下一个对象

    return next;
  }

  private boolean isClosed() {
    return status == CursorStatus.CLOSED || status == CursorStatus.CONSUMED;
  }

  private int getReadItemsCount() {
    return indexWithRowBound + 1;
  }
  // 简单的结果处理器 ResultHandler 结果处理器接口
  protected static class ObjectWrapperResultHandler<T> implements ResultHandler<T> {

    protected T result;
    protected boolean fetched; // 获取结果标识 ，用于标识result是否为空

    @Override // 从上下文结果集中取出结果并进行处理
    public void handleResult(ResultContext<? extends T> context) {
      this.result = context.getResultObject(); // 取出结果放入到自身结果中，没有做特殊处理
      context.stop(); //关闭结果上下文
      fetched = true; //标志位设置为true，已经获取到对象
    }
  }
  // 迭代器类
  protected class CursorIterator implements Iterator<T> {

    /** 下一个要返回的对象的持有人。  缓存下一个要返回的对象，在next操作中完成写入
     * Holder for the next object to be returned.
     */
    T object;

    /** 使用next（）返回的对象的索引，因此对用户可见。next方法中返回的对象的索引
     * Index of objects returned using next(), and as such, visible to users.
     */
    int iteratorIndex = -1;

    @Override
    public boolean hasNext() { // 判断是否存在下一个元素，如果存在的话，直接进行写入
      if (!objectWrapperResultHandler.fetched) {
        object = fetchNextUsingRowBound();
      }
      return objectWrapperResultHandler.fetched;
    }

    @Override
    public T next() { //取出结果
      // Fill next with object fetched from hasNext()
      T next = object;

      if (!objectWrapperResultHandler.fetched) { // 如果object没有值的话，尝试获取一个，获取之后就直接抛出异常了
        next = fetchNextUsingRowBound();
      }

      if (objectWrapperResultHandler.fetched) { // 和上面的判断必反的，走上面的就不走这个， 存在值的话，把值取走，然后将标志位设置为false
        objectWrapperResultHandler.fetched = false;
        object = null;
        iteratorIndex++;
        return next;
      }
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove element from Cursor");
    }
  }
}
