# EasyThread
<a href="http://www.methodscount.com/?lib=com.github.yjfnypeu%3AEasyThread%3A0.1"><img src="https://img.shields.io/badge/Methods count-61-e91e63.svg"/></a>

一款安全、轻巧、简单的线程池管理器

### 特性

- 可重命名线程名。方便出错后定位问题。
- 当线程出现异常。能自动将catch异常信息传递给用户，避免程序崩溃。
- 当任务启动时与任务运行完毕后。有分别的生命周期作为通知。

### 依赖

lastestVersion = [![](https://jitpack.io/v/yjfnypeu/EasyThread.svg)](https://jitpack.io/#yjfnypeu/EasyThread)

```groovy
// 添加jitPack仓库使用
maven { url 'https://jitpack.io' }

// 添加依赖
compile "com.github.yjfnypeu:EasyThread:$lastestVersion"
```

### 用法

使用方式分两步走：

- 第一步：

    创建EasyThread实例。每个EasyThread实例会持有一个独立的线程池。

```java
EasyThread executor = 
		EasyThread.Builder
            // 通过此三种方法指定所管理器所需要使用的线程池类型，对应Executors.newXXXThreadPool
            .fixed(2) | .cacheable() | .single()
            .priority(priority) //指定任务执行时所使用的线程优先级
            .name(DEFAULT_THREAD_NAME)// 指定子线程执行时所使用的线程名
            .callback(DEFAULT_CALLBACK) // 指定子线程执行时所使用的回调监听
            .build();
```

- 第二步：

	使用创建的EasyThread实例进行任务执行：

```java
executor.name(name)// 可分别对每次的执行任务进行重设线程名
    .callback(callback) // 可分别对每次的执行任务进行重设回调监听
    .execute(runnable) | .submit(callable) // 启动任务
```

- 线程执行回调监听器

	对于监听器回调函数运行所处线程。EasyThread内部有单独处理。
	1. 当在纯java环境下使用时，回调函数所处线程与任务执行线程一致。
	2. 当在Android环境下使用时，回调函数所处线程为主线程。

```java
ThreadCallback implements Callback {

    @Override
    public void onError(Thread thread, Throwable t) {
        // 当使用EasyThread启动后台任务后，若在子线程运行过程中。出现了异常。将会将异常错误 t 回调通知到此方法中通知用户
    }

    @Override
    public void onCompleted(Thread thread) {
        // 当使用EasyThread启动后台任务后，若子线程运行完毕。将会回调到此方法中通知用户
    }

    @Override
    public void onStart(Thread thread) {
        // 当子线程启动运行时，回调到此方法通知用户。
    }
}
```

### Example

对于程序来说。线程资源是宝贵的。为了避免创建过多额外的线程，所以建议对每个app。提供一个统一的管理器维护所有的线程池，如下所示：

```java
public final class ThreadManager {

    private final static EasyThread io;
    private final static EasyThread cache;
    private final static EasyThread calculator;
    private final static EasyThread file;

    public static EasyThread getIO () {
        return io;
    }

    public static EasyThread getCache() {
        return cache;
    }

    public static EasyThread getCalculator() {
        return calculator;
    }

    public static EasyThread getFile() {
        return file;
    }

    static {
        io = EasyThread.Builder.fixed(6).name("IO").priority(7).callback(new DefaultCallback()).build();
        cache = EasyThread.Builder.cacheable().name("cache").callback(new DefaultCallback()).build();
        calculator = EasyThread.Builder.fixed(4).name("calculator").priority(Thread.MAX_PRIORITY).callback(new DefaultCallback()).build();
        file = EasyThread.Builder.fixed(4).name("file").priority(3).callback(new DefaultCallback()).build();
    }

    private static class DefaultCallback implements Callback {

        @Override
        public void onError(Thread thread, Throwable t) {
            MyLog.e("Task with thread %s has occurs an error: %s", thread, t.getMessage());
        }

        @Override
        public void onCompleted(Thread thread) {
            MyLog.d("Task with thread %s completed", thread);
        }

        @Override
        public void onStart(Thread thread) {
            MyLog.d("Task with thread %s start running!", thread);
        }
    }
}
```
