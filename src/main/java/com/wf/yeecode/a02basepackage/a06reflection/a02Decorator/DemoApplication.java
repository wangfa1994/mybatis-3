package com.wf.yeecode.a02basepackage.a06reflection.a02Decorator;

/**
 * 装饰者模式
 *
 * 装饰器模式又称包装模式，是一种结构型模式。这种设计模式是指能够在一个类的基础上增加一个装饰类（也可以叫包装类），并在
 * 装饰类中增加一些新的特性和功能。这样，通过对原有类的包装，就可以在不改变原有类的情况下为原有类增加更多的功能。
 *
 * 装饰器模式在编程开发中经常使用。通常的使用场景是在一个核心基本类的基础上，提供大量的装饰类，从而使核心基本类经过不同
 * 的装饰类修饰后获得不同的功能
 *
 * 装饰类还有一个优点，就是可以叠加使用，即一个核心基本类可以被多个装饰类修饰，从而同时具有多个装饰类的功能
 *
 *
 */
public class DemoApplication {


  public static void main(String[] args) {
    Phone telephone = new Telephone();
    System.out.println("正常电话接收语音："+telephone.callIn());

    Phone phone = new PhoneRecordDecorator(telephone);
    System.out.println("开启录音的接收语音"+phone.callIn());
  }

}
