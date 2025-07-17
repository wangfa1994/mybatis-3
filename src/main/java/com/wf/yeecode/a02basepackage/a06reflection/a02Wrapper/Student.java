package com.wf.yeecode.a02basepackage.a06reflection.a02Wrapper;

import java.util.List;

public class Student {

  private String name;
  private Integer age;
  private List<Book> books;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public List<Book> getBooks() {
    return books;
  }

  public void setBooks(List<Book> books) {
    this.books = books;
  }
}
