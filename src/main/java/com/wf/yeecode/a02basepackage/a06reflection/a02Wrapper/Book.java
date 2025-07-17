package com.wf.yeecode.a02basepackage.a06reflection.a02Wrapper;

import java.math.BigDecimal;

public class Book {

  private String name;
  private BigDecimal price;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }
}
