package com.lzh.easythread;

import java.util.concurrent.Callable;

final class CallableWrapper<T> implements Callable<T> {
    private String name;
    private ErrorCallback callback;
    private Callable<T> proxy;

    CallableWrapper(String name, ErrorCallback callback, Callable<T> proxy) {
        this.name = name;
        this.callback = callback;
        this.proxy = proxy;
    }

    @Override
    public T call() throws Exception {
        Tools.resetThead(Thread.currentThread(),name,callback);
        return proxy.call();
    }
}
