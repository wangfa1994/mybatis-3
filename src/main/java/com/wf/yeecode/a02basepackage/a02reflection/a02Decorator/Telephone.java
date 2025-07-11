package com.wf.yeecode.a02basepackage.a02reflection.a02Decorator;

public class Telephone implements Phone{
  @Override
  public String callIn() {
    System.out.println("接收语音");
    return "get info";
  }

  @Override
  public Boolean callOut(String callInfo) {
    System.out.println("发送语音："+callInfo);
    return Boolean.TRUE;
  }



}
