package com.lzh.easythread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public final class EasyThread {
    private ExecutorService pool;
    private String defName;
    private Callback defCallback;

    private String name;
    private Callback callback;

    private EasyThread(int type, int size, int priority, String name, Callback callback) {
        this.pool = createPool(type, size, priority);
        this.defName = name;
        this.defCallback = callback;
    }

    public EasyThread name (String name) {
        this.name = name;
        return this;
    }

    public EasyThread callback (Callback callback) {
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
        return Tools.isEmpty(name) ? defName : name;
    }

    private Callback getCallback () {
        Callback used = callback == null ? defCallback : callback;
        if (Tools.isAndroid) {
            return new AndroidCallback(used);
        } else {
            return used;
        }
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

    private static class AndroidCallback implements Callback {
        private static Handler main = new Handler(Looper.getMainLooper());
        private Callback delegate;

        AndroidCallback(Callback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onError(final Thread thread, final Throwable t) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    if (delegate != null) {
                        delegate.onError(thread, t);
                    }
                }
            });
        }

        @Override
        public void onCompleted(final Thread thread) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    if (delegate != null) {
                        delegate.onCompleted(thread);
                    }
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
        Callback callback;

        private Builder(int size,  int type) {
            this.size = size;
            this.type = type;
        }

        /**
         * create a cacheable thread manager to used
         * @return Builder instance
         */
        public static Builder cacheable () {
            return new Builder(0, TYPE_CACHEABLE);
        }

        /**
         * create a thread manager with a limit size to used
         * @param size size
         * @return Builder instance
         */
        public static Builder fixed (int size) {
            return new Builder(size, TYPE_FIXED);
        }

        /**
         * create a thread manager with single thread to used
         * @return Builder instance
         */
        public static Builder single () {
            return new Builder(0, TYPE_SINGLE);
        }

        /**
         * Set a default name for thread manager to used
         * @return  itself
         */
        public Builder name (String name) {
            if (!Tools.isEmpty(name)) {
                this.name = name;
            }
            return this;
        }

        /**
         * Set a default priority for thread manager to used
         * @return  itself
         */
        public Builder priority (int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Set a default callback for thread manager
         * @return  itself
         */
        public Builder callback (Callback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Create a thread manager to used with some configurations.
         * @return  EasyThread instance
         */
        public EasyThread build () {
            priority = Math.max(Thread.MIN_PRIORITY, priority);
            priority = Math.min(Thread.MAX_PRIORITY, priority);

            size = Math.max(0,size);
            if (Tools.isEmpty(name)) {
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
