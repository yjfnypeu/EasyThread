package com.lzh.easythread;

/**
 * A Runnable Wrapper to delegate {@link Runnable#run()}
 */
final class RunnableWrapper implements Runnable {

    private String name;
    private Callback callback;
    private Runnable proxy;

    RunnableWrapper(String name, Callback callback, Runnable proxy) {
        this.name = name;
        this.callback = callback;
        this.proxy = proxy;
    }

    @Override
    public void run() {
        if (callback != null) {
            callback.onStart(Thread.currentThread());
        }
        Tools.resetThread(Thread.currentThread(),name,callback);
        proxy.run();
        if (callback != null)  {
            callback.onCompleted(Thread.currentThread());
        }
    }
}
