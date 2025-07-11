package com.wf.yeecode.a02basepackage.a02reflection.a02Decorator;

public class PhoneRecordDecorator implements Phone{

  private Phone phone;

  public PhoneRecordDecorator(Phone phone) {
    this.phone = phone;
  }


  @Override
  public String callIn() {
    System.out.println("启动录音");
    String info = phone.callIn();
    System.out.println("录音完成");
    return info;
  }

  @Override
  public Boolean callOut(String callInfo) {
    return null;
  }
}
