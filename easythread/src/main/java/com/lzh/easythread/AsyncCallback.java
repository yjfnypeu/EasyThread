package com.lzh.easythread;

/**
 * Async callback class.
 * @author haoge on 2018/2/9.
 */
public interface AsyncCallback<T> {
    void onSuccess(T t);
    void onFailed(Throwable t);
}
