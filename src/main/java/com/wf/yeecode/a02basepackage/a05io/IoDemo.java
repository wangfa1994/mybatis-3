package com.wf.yeecode.a02basepackage.a05io;

import org.apache.ibatis.io.DefaultVFS;

public class IoDemo {

  public static void main(String[] args) throws Exception {

    DefaultVFS defaultVFS  = new DefaultVFS();
    boolean valid = defaultVFS.isValid();
    defaultVFS.list("");

  }
}
