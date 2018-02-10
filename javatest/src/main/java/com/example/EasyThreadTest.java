package com.example;

import com.lzh.easythread.AsyncCallback;
import com.lzh.easythread.Callback;
import com.lzh.easythread.EasyThread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

        EasyThread pool = EasyThread.Builder.single().name("TestSubmit").build();
        Future<Integer> submit = pool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1000);
                return 10;
            }
        });

        int result;
        try {
            result = submit.get(2, TimeUnit.SECONDS);
        } catch (Throwable e) {
            result = 0;
        }

        System.out.println("result = " + result);

        easyThread.async(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "async task result";
            }
        }, new AsyncCallback<String>() {
            @Override
            public void onSuccess(String response) {
                System.out.println("response = [" + response + "]");
            }

            @Override
            public void onFailed(Throwable t) {
                System.out.println("t = [" + t + "]");
            }
        });
    }
}