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
