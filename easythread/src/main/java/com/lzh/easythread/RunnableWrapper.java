package com.lzh.easythread;

final class RunnableWrapper implements Runnable {

    private String name;
    private ErrorCallback callback;
    private Runnable proxy;

    RunnableWrapper(String name, ErrorCallback callback, Runnable proxy) {
        this.name = name;
        this.callback = callback;
        this.proxy = proxy;
    }

    @Override
    public void run() {
        Tools.resetThead(Thread.currentThread(),name,callback);
        proxy.run();
    }
}
