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

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class EasyThread implements Executor{
    private ExecutorService pool;
    // ==== There are default configs
    private String defName;// default thread name
    private Callback defCallback;// default thread callback
    private Executor defDeliver;// default thread deliver

    // to make sure no conflict for multi thread config.
    private ThreadLocal<Configs> local;

    private EasyThread(int type, int size, int priority, String name, Callback callback, Executor deliver, ExecutorService pool) {
        if (pool == null) {
            pool = createPool(type, size, priority);
        }
        this.pool = pool;
        this.defName = name;
        this.defCallback = callback;
        this.defDeliver = deliver;
        this.local = new ThreadLocal<>();
    }

    /**
     * Set thread name for current task. if not set. the default name should be used.
     * @param name thread name
     * @return EasyThread
     */
    public EasyThread setName(String name) {
        getLocalConfigs().name = name;
        return this;
    }

    /**
     * Set thread callback for current task, if not set, the default callback should be used.
     * @param callback thread callback
     * @return EasyThread
     */
    public EasyThread setCallback (Callback callback) {
        getLocalConfigs().callback = callback;
        return this;
    }

    /**
     * Set the delay time for current task.
     *
     * <p>Attention: it only take effects when your thread pool is create by {@link Builder#createScheduled(int)}</p>
     * @param time time length
     * @param unit time unit
     * @return EasyThread
     */
    public EasyThread setDelay (long time, TimeUnit unit) {
        long delay = unit.toMillis(time);
        getLocalConfigs().delay = delay;
        return this;
    }

    /**
     * Set the thread deliver for current task. if not set, the default deliver should be used.
     * @param deliver thread deliver
     * @return EasyThread
     */
    public EasyThread setDeliver(Executor deliver){
        getLocalConfigs().deliver = deliver;
        return this;
    }

    /**
     * Launch task
     * @param runnable task
     */
    @Override
    public void execute (Runnable runnable) {
        runnable = new RunnableWrapper(getLocalConfigs()).setRunnable(runnable);
        pool.execute(runnable);
        resetLocalConfigs();
    }

    /**
     * Launch async task, and the callback are used for receive the result of callable task.
     * @param callable callable
     * @param callback callback
     * @param <T> type
     */
    public <T> void async(Callable<T> callable, AsyncCallback<T> callback) {
        Configs configs = getLocalConfigs();
        configs.asyncCallback = callback;
        Runnable runnable = new RunnableWrapper(configs)
                .setCallable(callable);
        pool.execute(runnable);
        resetLocalConfigs();
    }

    /**
     * Launch task
     * @param callable callable
     * @param <T> type
     * @return {@link Future}
     */
    public <T> Future<T> submit (Callable<T> callable) {
        Future<T> result;
        callable = new CallableWrapper<>(getLocalConfigs(), callable);
        result = pool.submit(callable);
        resetLocalConfigs();
        return result;
    }

    /**
     * get thread pool that be created.
     * @return thread pool
     */
    public ExecutorService getExecutor() {
        return pool;
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

    private synchronized void resetLocalConfigs() {
        local.set(null);
    }

    private synchronized Configs getLocalConfigs() {
        Configs configs = local.get();
        if (configs == null) {
            configs = new Configs();
            configs.name = defName;
            configs.callback = defCallback;
            configs.deliver = defDeliver;
            local.set(configs);
        }
        return configs;
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
        Executor deliver;
        ExecutorService pool;

        private Builder(int size,  int type, ExecutorService pool) {
            this.size = Math.max(1, size);
            this.type = type;
            this.pool = pool;
        }

        public static Builder create(ExecutorService pool) {
            return new Builder(1, TYPE_SINGLE, pool);
        }

        /**
         * Create thread pool by <b>Executors.newCachedThreadPool()</b>
         * @return Builder itself
         */
        public static Builder createCacheable() {
            return new Builder(0, TYPE_CACHEABLE, null);
        }

        /**
         * Create thread pool by <b>Executors.newFixedThreadPool()</b>
         * @param size thread size
         * @return Builder itself
         */
        public static Builder createFixed(int size) {
            return new Builder(size, TYPE_FIXED, null);
        }

        /**
         * Create thread pool by <b>Executors.newScheduledThreadPool()</b>
         * @param size thread size
         * @return Builder itself
         */
        public static Builder createScheduled(int size) {
            return new Builder(size, TYPE_SCHEDULED, null);
        }

        /**
         * Create thread pool by <b>Executors.newSingleThreadPool()</b>
         *
         * @return Builder itself
         */
        public static Builder createSingle() {
            return new Builder(0, TYPE_SINGLE, null);
        }

        /**
         * Set default thread name to used.
         * @param name default thread name
         * @return Builder itself
         */
        public Builder setName (String name) {
            if (!Tools.isEmpty(name)) {
                this.name = name;
            }
            return this;
        }

        /**
         * Set default thread priority to used.
         * @param priority thread priority
         * @return  itself
         */
        public Builder setPriority (int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Set default thread callback to used.
         * @param callback thread callback
         * @return itself
         */
        public Builder setCallback (Callback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Set default thread deliver to used.
         * @param deliver default thread deliver
         * @return itself
         */
        public Builder setDeliver (Executor deliver) {
            this.deliver = deliver;
            return this;
        }

        /**
         * Create a thread manager to used with some configurations.
         * @return  EasyThread instance
         */
        public EasyThread build () {
            priority = Math.max(Thread.MIN_PRIORITY, priority);
            priority = Math.min(Thread.MAX_PRIORITY, priority);

            size = Math.max(1, size);
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
                    default:
                        name = "EasyThread";
                }
            }

            if (deliver == null) {
                if (Tools.isAndroid) {
                    deliver = AndroidDeliver.getInstance();
                } else {
                    deliver = JavaDeliver.getInstance();
                }
            }

            return new EasyThread(type, size, priority, name, callback, deliver, pool);
        }
    }
}
