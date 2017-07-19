/*
 * Copyright (C) 2017 Haoge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzh.easythread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class EasyThread implements Executor{
    private ExecutorService pool;
    private String defName;// default thread name.
    private Callback defCallback;// default thread callback.

    private String name;// a temp thread name. just used for current task.
    private Callback callback;// a temp thread callback. just used for current task.
    private long delay;// the delay time for current task. only the task is created with type of scheduled. it will be worked.

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

    public EasyThread delay (long time, TimeUnit unit) {
        delay = unit.toMillis(time);
        return this;
    }

    @Override
    public void execute (Runnable runnable) {
        if (delay > 0 && pool instanceof ScheduledExecutorService) {
            ((ScheduledExecutorService)pool).schedule(runnable, delay, TimeUnit.MILLISECONDS);
        } else {
            pool.execute(new RunnableWrapper(getName(), getCallback(), runnable));
        }
        release();
    }

    public <T> Future<T> submit (Callable<T> callable) {
        Future<T> result;
        if (delay > 0 && pool instanceof ScheduledExecutorService) {
            result = ((ScheduledExecutorService)pool).schedule(callable, delay, TimeUnit.MILLISECONDS);
        } else {
            result = pool.submit(new CallableWrapper<>(getName() ,getCallback(),callable));
        }
        release();
        return result;
    }

    private void release() {
        this.name = null;
        this.callback = null;
        this.delay = -1;
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
            case Builder.TYPE_SCHEDULED:
                return Executors.newScheduledThreadPool(size, new DefaultFactory(priority));
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

        @Override
        public void onStart(final Thread thread) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    if (delegate != null) {
                        delegate.onStart(thread);
                    }
                }
            });
        }
    }

    public static class Builder {
        final static int TYPE_CACHEABLE = 0;
        final static int TYPE_FIXED = 1;
        final static int TYPE_SINGLE = 2;
        final static int TYPE_SCHEDULED = 3;

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
         * Create a cacheable thread manager to used:<b>Executors.newCachedThreadPool()</b>
         * @return Builder itself
         */
        public static Builder cacheable () {
            return new Builder(0, TYPE_CACHEABLE);
        }

        /**
         * Create a thread manager with a limit size to used:<b>Executors.newFixedThreadPool()</b>
         * @param size size
         * @return Builder itself
         */
        public static Builder fixed (int size) {
            return new Builder(size, TYPE_FIXED);
        }

        /**
         * Create a thread manager with a scheduled thread pool: <b>Executors.newScheduledThreadPool()</b>
         * @param size Thread size.
         * @return Builder itself
         */
        public static Builder scheduled (int size) {
            return new Builder(size, TYPE_SCHEDULED);
        }

        /**
         * create a thread manager with single thread to used
         *
         * @return Builder itself
         */
        public static Builder single () {
            return new Builder(0, TYPE_SINGLE);
        }

        /**
         * Set a default name for thread manager to used
         * @param name The thread name.
         * @return Builder itself
         */
        public Builder name (String name) {
            if (!Tools.isEmpty(name)) {
                this.name = name;
            }
            return this;
        }

        /**
         * Set a default priority for thread manager to used
         * @param priority The thread priority
         * @return  itself
         */
        public Builder priority (int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Set a default callback for thread manager
         * @param callback The callback
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
