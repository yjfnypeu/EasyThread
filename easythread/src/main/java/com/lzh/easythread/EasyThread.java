package com.lzh.easythread;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public final class EasyThread {
    private ExecutorService pool;
    private int defPriority;
    private String defName;
    private ErrorCallback defCallback;

    private String name;
    private ErrorCallback callback;

    private EasyThread(int type, int size, int priority, String name, ErrorCallback callback) {
        this.pool = createPool(type, size, priority);
        this.defPriority = priority;
        this.defName = name;
        this.defCallback = callback;
    }

    public EasyThread name (String name) {
        this.name = name;
        return this;
    }

    public EasyThread callback (ErrorCallback callback) {
        this.callback = callback;
        return this;
    }

    public void execute (Runnable runnable) {
        pool.execute(new RunnableWrapper(getName(), getCallback(), runnable));
        release();
    }

    public <T> Future<T> submit (Callable<T> callable) {
        Future<T> future = pool.submit(new CallableWrapper<>(getName() ,getCallback(),callable));
        release();
        return future;
    }

    private void release() {
        this.name = null;
        this.callback = null;
    }

    private String getName () {
        return TextUtils.isEmpty(name) ? defName : name;
    }

    private ErrorCallback getCallback () {
        return new DefaultCallback(callback == null ? defCallback : callback);
    }

    private ExecutorService createPool(int type, int size, int priority) {
        switch (type) {
            case Builder.TYPE_CACHEABLE:
                return Executors.newCachedThreadPool(new DefaultFactory(priority));
            case Builder.TYPE_FIXED:
                return Executors.newFixedThreadPool(size, new DefaultFactory(priority));
            case Builder.TYPE_SINGLE:
            default:
                return Executors.newSingleThreadExecutor(new DefaultFactory(priority));
        }
    }

    private static class DefaultFactory implements ThreadFactory {

        private int priority;
        DefaultFactory(int priority) {
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(priority);
            return thread;
        }
    }

    private static class DefaultCallback implements ErrorCallback {
        private static Handler main = new Handler(Looper.getMainLooper());
        private ErrorCallback delegate;

        DefaultCallback(ErrorCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onError(final Throwable t) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    delegate.onError(t);
                }
            });
        }
    }

    public static class Builder {
        final static int TYPE_CACHEABLE = 0;
        final static int TYPE_FIXED = 1;
        final static int TYPE_SINGLE = 2;

        int type;
        int size;
        int priority = Thread.NORM_PRIORITY;
        String name;
        ErrorCallback callback;

        private Builder(int size,  int type) {
            this.size = size;
            this.type = type;
        }

        public static Builder cacheable () {
            return new Builder(0, TYPE_CACHEABLE);
        }

        public static Builder fixed (int size) {
            return new Builder(size, TYPE_FIXED);
        }

        public static Builder single () {
            return new Builder(0, TYPE_SINGLE);
        }

        public Builder name (String name) {
            if (!TextUtils.isEmpty(name)) {
                this.name = name;
            }
            return this;
        }

        public Builder priority (int priority) {
            this.priority = priority;
            return this;
        }

        public Builder callback (ErrorCallback callback) {
            this.callback = callback;
            return this;
        }

        public EasyThread build () {
            priority = Math.max(Thread.MIN_PRIORITY, priority);
            priority = Math.min(Thread.MAX_PRIORITY, priority);

            size = Math.max(0,size);
            if (TextUtils.isEmpty(name)) {
                // set default thread name
                switch (type) {
                    case TYPE_CACHEABLE:
                        name = "CACHEABLE";
                        break;
                    case TYPE_FIXED:
                        name = "FIXED";
                        break;
                    case TYPE_SINGLE:
                        name = "SINGLE";
                        break;
                }
            }
            return new EasyThread(type,size,priority,name,callback);
        }
    }
}
