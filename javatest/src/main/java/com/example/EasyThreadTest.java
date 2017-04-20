package com.example;

import com.lzh.easythread.Callback;
import com.lzh.easythread.EasyThread;

public class EasyThreadTest {

    public static void main(String[] args) {
        testExecute();
    }

    private static void testExecute () {
        EasyThread easyThread = EasyThread.Builder.single()
                .name("test")
                .callback(new Callback() {
                    @Override
                    public void onError(Thread thread, Throwable t) {
                        System.out.println("thread:" + Thread.currentThread());
                        System.out.println("线程出错");
                    }

                    @Override
                    public void onCompleted(Thread thread) {
                        System.out.println("thread:" + Thread.currentThread());
                        System.out.println("线程完成");
                    }

                    @Override
                    public void onStart(Thread thread) {
                        System.out.println("thread:" + Thread.currentThread());
                        System.out.println("线程启动");
                    }
                }).build();
        easyThread.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程运行");
                throw new RuntimeException("故意的");
            }
        });
    }
}