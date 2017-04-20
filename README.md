# EasyThread
<a href="http://www.methodscount.com/?lib=com.github.yjfnypeu%3AEasyThread%3A0.1"><img src="https://img.shields.io/badge/Methods count-61-e91e63.svg"/></a>

一款简单易用的线程池管理器

### 依赖

lastestVersion = [![](https://jitpack.io/v/yjfnypeu/EasyThread.svg)](https://jitpack.io/#yjfnypeu/EasyThread)
```groovy
// 添加jitPack仓库使用
maven { url 'https://jitpack.io' }

// 添加依赖
compile "com.github.yjfnypeu:EasyThread:$lastestVersion"
```

### 用法

```java
// 1. 使用Builder创建EasyThread实例
EasyThread executor = EasyThread.Builder
                        // 通过此三种方法指定所管理器所需要使用的线程池类型，对应Executors.newXXXThreadPool
                        .fixed(2) | .cacheable() | .single()
                        .priority(priority) //指定子线程执行时所使用的线程优先级
                        .name(DEFAULT_THREAD_NAME)// 指定子线程执行时所使用的线程名
                        .callback(DEFAULT_CALLBACK) // 指定子线程执行时所使用的回调监听
                        .build();

// 2. 后续使用所创建出的管理器。进行线程操作：

executor.name(name)// 可分别对每次的执行任务进行重设线程名
    .callback(callback) // 可分别对每次的执行任务进行重设回调监听
    .execute(runnable) | .submit(callable) // 启动任务
```

```
// EasyThread设置的回调监听分为两个方法：

// 对于回调方法所运行的线程。当使用纯java环境运行时。回调方法所处线程即当前后台任务所处线程
// 当使用android 环境运行时，回调方法所处线程为主线程。方便更新UI
ThreadCallback implements Callback {

        @Override
        public void onError(Thread thread, Throwable t) {
            // 当使用EasyThread启动后台任务后，若在子线程运行过程中。出现了异常。将会将异常错误 t 回调通知到此方法中通知用户
        }

        @Override
        public void onCompleted(Thread thread) {
            // 当使用EasyThread启动后台任务后，若子线程运行完毕。将会回调到此方法中通知用户
        }
    }
```