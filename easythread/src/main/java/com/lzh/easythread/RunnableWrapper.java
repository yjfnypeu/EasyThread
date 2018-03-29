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

final class RunnableWrapper implements Runnable {

    private String name;
    private CallbackDelegate delegate;
    private Runnable runnable;
    private Callable callable;

    RunnableWrapper(Configs configs) {
        this.name = configs.name;
        this.delegate = new CallbackDelegate(configs.callback, configs.deliver, configs.asyncCallback);
    }

    RunnableWrapper setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    RunnableWrapper setCallable(Callable callable) {
        this.callable = callable;
        return this;
    }

    @Override
    public void run() {
        Thread current = Thread.currentThread();
        Tools.resetThread(current, name, delegate);
        delegate.onStart(name);

        // avoid NullPointException
        if (runnable != null) {
            runnable.run();
        } else if (callable != null) {
            try {
                Object result = callable.call();
                delegate.onSuccess(result);
            } catch (Exception e) {
                delegate.onError(name, e);
            }
        }
        delegate.onCompleted(name);
    }
}
