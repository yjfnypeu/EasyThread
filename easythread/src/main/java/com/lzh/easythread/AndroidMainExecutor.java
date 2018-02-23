package com.lzh.easythread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Adapter for <b>Android platform</b>.
 * @author haoge
 */
final class AndroidMainExecutor implements Executor {

    private static AndroidMainExecutor instance = new AndroidMainExecutor();
    private Handler main = new Handler(Looper.getMainLooper());

    static AndroidMainExecutor getInstance() {
        return instance;
    }

    @Override
    public void execute(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }

        main.post(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
