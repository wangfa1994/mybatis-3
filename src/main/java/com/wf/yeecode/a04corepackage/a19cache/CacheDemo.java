package com.wf.yeecode.a04corepackage.a19cache;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.impl.PerpetualCache;

public class CacheDemo {

  public static void main(String[] args) throws InterruptedException {
    Cache perpetualCache = new PerpetualCache("id");
    BlockingCache blockingCache = new BlockingCache(perpetualCache);

    Thread thread = new Thread(() -> {
      Object object = blockingCache.getObject("wang");
      if(object == null){
        // 去数据库查询放入
          try {
              Thread.sleep(10000);
          } catch (InterruptedException e) {
              throw new RuntimeException(e);
          }
          blockingCache.putObject("wang","fa");
      }
    });

    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {

        Object object = blockingCache.getObject("wang");
        System.out.println(object);
      }
    });
    thread.start();
    Thread.sleep(50);
    thread1.start();




    //perpetualCache();
  }

  private static void perpetualCache() {
    Cache perpetualCache = new PerpetualCache("id");

    perpetualCache.putObject("wang","fa");

    Object object = perpetualCache.getObject("wang");
    System.out.println(object);
  }
}
