package com.lzh.easythread;

import java.util.concurrent.Executor;

final class NotSwitchExecutor implements Executor {

    private static NotSwitchExecutor instance = new NotSwitchExecutor();

    static NotSwitchExecutor getInstance() {
        return instance;
    }

    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
