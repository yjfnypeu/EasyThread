package com.lzh.easythread;

final class Tools {

    static void resetThead (Thread thread, String name, final ErrorCallback callback) {
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
        thread.setName(name);
    }
}
