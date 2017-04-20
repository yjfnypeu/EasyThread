package com.lzh.easythread;

import java.util.concurrent.Callable;
/**
 * A Callable Wrapper to delegate {@link Callable#call()}
 */
final class CallableWrapper<T> implements Callable<T> {
    private String name;
    private Callback callback;
    private Callable<T> proxy;

    CallableWrapper(String name, Callback callback, Callable<T> proxy) {
        this.name = name;
        this.callback = callback;
        this.proxy = proxy;
    }

    @Override
    public T call() throws Exception {
        Tools.resetThread(Thread.currentThread(),name,callback);
        if (callback != null) {
            callback.onStart(Thread.currentThread());
        }

        // avoid NullPointException
        T t = proxy == null ? null : proxy.call();
        if (callback != null)  {
            callback.onCompleted(Thread.currentThread());
        }
        return t;
    }
}
