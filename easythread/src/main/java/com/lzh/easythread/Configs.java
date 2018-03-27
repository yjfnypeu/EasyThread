package com.lzh.easythread;

import java.util.concurrent.Executor;

/**
 * Store some configurations for current task.
 * @author haoge on 2018/3/27.
 */
final class Configs {
    String name;// thread name
    Callback callback;// thread callback
    long delay;// delay time
    Executor deliver;// thread deliver
    AsyncCallback asyncCallback;
}
