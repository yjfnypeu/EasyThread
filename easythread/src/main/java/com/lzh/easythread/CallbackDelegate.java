package com.lzh.easythread;

import java.util.concurrent.Executor;

/**
 * The callback delegate class.
 *
 * @author haoge on 2018/2/9.
 */
final class CallbackDelegate implements Callback, AsyncCallback {

    private Callback callback;
    private AsyncCallback async;
    private Executor deliver;

    CallbackDelegate(Callback callback, Executor deliver, AsyncCallback async) {
        this.callback = callback;
        this.deliver = deliver;
        this.async = async;
    }

    @Override
    public void onSuccess(final Object o) {
        if (async == null) return;
        deliver.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //noinspection unchecked
                    async.onSuccess(o);
                } catch (Throwable t) {
                    onFailed(t);
                }
            }
        });
    }

    @Override
    public void onFailed(final Throwable t) {
        if (async == null) return;
        deliver.execute(new Runnable() {
            @Override
            public void run() {
                async.onFailed(t);
            }
        });
    }

    @Override
    public void onError(final Thread thread, final Throwable t) {
        onFailed(t);

        if (callback == null) return;
        deliver.execute(new Runnable() {
            @Override
            public void run() {
                callback.onError(thread, t);
            }
        });
    }

    @Override
    public void onCompleted(final Thread thread) {
        if (callback == null) return;
        deliver.execute(new Runnable() {
            @Override
            public void run() {
                callback.onCompleted(thread);
            }
        });
    }

    @Override
    public void onStart(final Thread thread) {
        if (callback == null) return;
        deliver.execute(new Runnable() {
            @Override
            public void run() {
                callback.onStart(thread);
            }
        });
    }
}
