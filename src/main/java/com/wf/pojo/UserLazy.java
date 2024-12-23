package com.wf.pojo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserLazy implements Serializable {

    private Integer id;

    private String username;

    //表示用户关联的订单
    private List<Order> orderList = new ArrayList<>();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<Order> getOrderList() {
    return orderList;
  }

  public void setOrderList(List<Order> orderList) {
    this.orderList = orderList;
  }
}
