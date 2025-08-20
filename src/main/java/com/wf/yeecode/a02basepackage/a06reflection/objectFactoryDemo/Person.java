package com.wf.yeecode.a02basepackage.a06reflection.objectFactoryDemo;



import java.util.List;

public class Person {

  private String name;

  private List<String> bookList;

  public Person() {
  }

  public Person(String name, List<String> bookList) {
    this.name = name;
    this.bookList = bookList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getBookList() {
    return bookList;
  }

  public void setBookList(List<String> bookList) {
    this.bookList = bookList;
  }
}
