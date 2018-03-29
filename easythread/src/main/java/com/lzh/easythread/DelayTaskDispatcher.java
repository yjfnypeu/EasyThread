package com.lzh.easythread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * The class to launch the delay-task with a core Scheduled pool.
 * @author haoge on 2018/3/29.
 */
final class DelayTaskDispatcher {
    private ScheduledExecutorService dispatcher;
    private static DelayTaskDispatcher instance = new DelayTaskDispatcher();
    private DelayTaskDispatcher() {
        dispatcher = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("Delay-Task-Dispatcher");
                thread.setPriority(Thread.MAX_PRIORITY);
                return thread;
            }
        });
    }
    static DelayTaskDispatcher get() {
        return instance;
    }

    void postDelay(long delay, final ExecutorService pool, final Runnable task) {
        if (delay == 0) {
            pool.execute(task);
            return;
        }

        dispatcher.schedule(new Runnable() {
            @Override
            public void run() {
                pool.execute(task);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

}
