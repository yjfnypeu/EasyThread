package com.example;

import com.lzh.easythread.Callback;
import com.lzh.easythread.EasyThread;

public class EasyThreadTest {

    public static void main(String[] args) {
        testExecute();
    }

    private static class SimpleCallback implements Callback {

        @Override
        public void onError(Thread thread, Throwable t) {
            System.out.println("thread = [" + thread.getName() + "], t = [" + t.getMessage() + "]");
        }

        @Override
        public void onCompleted(Thread thread) {
            System.out.println("onCompleted " + "thread = [" + thread.getName() + "]");
            System.out.println("Current Thread is " + Thread.currentThread().getName());
            System.out.println();
        }

        @Override
        public void onStart(Thread thread) {
            System.out.println("onStart " + "thread = [" + thread.getName() + "]");
            System.out.println("Current Thread is " + Thread.currentThread().getName());
            System.out.println();
        }
    }

    private static void testExecute () {
        EasyThread single = EasyThread.Builder.single()
                .name("single")
//                .callback(new SimpleCallback())
                .build();

        EasyThread fixed = EasyThread.Builder.fixed(2)
                .deliver(single)
                .name("fixed")
                .callback(new SimpleCallback())
                .build();

        fixed.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task running");
            }
        });

//        EasyThread easyThread = EasyThread.Builder.single()
//                .name("test")
//                .callback(new Callback() {
//                    @Override
//                    public void onError(Thread thread, Throwable t) {
//                        System.out.println("thread:" + Thread.currentThread());
//                        System.out.println("线程出错");
//                    }
//
//                    @Override
//                    public void onCompleted(Thread thread) {
//                        System.out.println("thread:" + Thread.currentThread());
//                        System.out.println("线程完成");
//                    }
//
//                    @Override
//                    public void onStart(Thread thread) {
//                        System.out.println("thread:" + Thread.currentThread());
//                        System.out.println("线程启动");
//                    }
//                }).build();
//        easyThread.execute(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("子线程运行");
//                throw new RuntimeException("故意的");
//            }
//        });
//
//        EasyThread pool = EasyThread.Builder.single().name("TestSubmit").build();
//        Future<Integer> submit = pool.submit(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                Thread.sleep(1000);
//                return 10;
//            }
//        });
//
//        int result;
//        try {
//            result = submit.get(2, TimeUnit.SECONDS);
//        } catch (Throwable e) {
//            result = 0;
//        }
//
//        System.out.println("result = " + result);
//
//        easyThread.async(new Callable<String>() {
//            @Override
//            public String call() throws Exception {
//                return "async task result";
//            }
//        }, new AsyncCallback<String>() {
//            @Override
//            public void onSuccess(String response) {
//                System.out.println("response = [" + response + "]");
//            }
//
//            @Override
//            public void onFailed(Throwable t) {
//                System.out.println("t = [" + t + "]");
//            }
//        });
    }
}