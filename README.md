# EasyThread ![travis-ci](https://travis-ci.org/yjfnypeu/EasyThread.svg?branch=master)
<a href="http://www.methodscount.com/?lib=com.github.yjfnypeu%3AEasyThread%3A0.1"><img src="https://img.shields.io/badge/Methods count-61-e91e63.svg"/></a>

一款安全、轻巧、简单的线程池管理器

### 特性

- **极致精简**:方法数不过百
- 可重命名线程名。方便出错后定位问题。
- 当线程出现异常。能自动将catch异常信息传递给用户，避免程序崩溃。
- 自带线程切换功能：指定任务执行后，在哪个线程中进行用户通知。
- 当任务启动时与任务运行完毕后。有分别的生命周期作为通知。
- 支持延迟任务以及异步回调任务

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

```
EasyThread easyThread = EasyThread.Builder
            // 通过此三种方法指定所管理器所需要使用的线程池类型，对应Executors.newXXXThreadPool
            .fixed(2) | .cacheable() | .single()
            .priority(priority) //指定任务执行时所使用的线程优先级
            .name(DEFAULT_THREAD_NAME)// 指定子线程执行时所使用的线程名
            .callback(DEFAULT_CALLBACK) // 指定子线程执行时所使用的回调监听
            .build();
```

- 第二步：

使用创建的EasyThread实例进行任务执行：

```
easyThread.name(name)// 可分别对每次的执行任务进行重设线程名
    .delay(time, unit)// 可指定延迟执行时间
    .callback(callback) // 可分别对每次的执行任务进行重设回调监听
    .execute(runnable) | .submit(callable) | .async(callable, asyncCallback)// 启动任务
```

EasyThread支持执行四种任务：

#### 1. 普通Runnable任务

```
easyThread.execute(new Runnable(){
    @Override
    public void run() {
        // do something.
    }
});
```

#### 2. 普通Callable任务

```
Future task = easyThread.submit(new Callback<User>(){
    @Override
    public void call() throws Exception {
        // do something
        return user;
    }
})
User result = task.get();
```
#### 3. 异步回调任务

```
// 异步执行任务
Callable<User> callable = new Callable<User>(){
    @Override
    public void call() throws Exception {
        // do something
        return user;
    }
}

// 异步回调
AsyncCallback<User> async = new AsyncCallback<User>() {
    @Override
    public void onSuccess(User user) {
        // notify success;
    }

    @Override
    public void onFailed(Throwable t) {
        // notify failed.
    }
};

// 启动异步任务
easyThread.async(callable, async)
```

#### 4. 延迟后台任务

```
// 在启动任务前，调用delay方法，指定延迟时间即可
easyThread.delay(time, unit)
    .execute(runnable);
```

**e.g 延迟3秒启动执行任务**

```
easyThread.delay(3, TimeUnit.SECONDS)
        .execute(task);
```

### 回调通知接口

EasyThread提供两种回调接口：**Callback与AsyncCallback**:

#### 1. Callback

所有线程任务共有的，用于对当前的任务状态进行监听、通知用户。

```java
public interface Callback {
	// 当任务启动时。通知到此。
	void onStart (Thread thread);
	// 当任务执行过程中出现异常时，捕获住异常并通知到此。
	void onError (Thread thread, Throwable t);
	// 当任务执行一切顺利。执行完毕后，通知到此。
	void onCompleted (Thread thread);
}
```

- 配置方式：

```
// 配置默认状态回调通知：
EasyThread.Builder.single().callback(callback);

// 配置本次任务执行时的回调通知：
easyThread.callback(callback);
```

- **AsyncCallback**:

异步回调接口只存在于执行异步任务操作时。

```
public interface AsyncCallback<T> {
	// 执行异步任务完成。将结果回调返回
	void onSuccess(T t);
	// 执行异步任务失败，将失败异常回调返回
	void onFailed(Throwable t);
}
```

- 配置方式：参考上方的**异步回调任务**示例代码

### 回调任务派发器

所谓派发器。就是用于指定线程任务的回调通知运行在哪个线程中的一种机制。

比如说在Android平台，很常见的就是回调时需要进行界面通知，所以这个时候就需要回调通知运行在UI线程。便于操作。

**配置方式**

- **配置默认派发器**:

```
EasyThread executor = EasyThread.Builder
	...// 创建Builder
	.deliver(deliver);
```

- **配置当前任务使用的派发器**：

```
easyThread.deliver(deliver);
```

在默认条件下(即未配置额外的派发器时)，在Android或者Java平台，分别适配了不同的回调派发逻辑：

- **在纯java环境下：回调方法所运行的线程与任务执行线程一致**
- **在Android环境：回调方法默认运行于主线程**

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

### 联系作者

Email:470368500@qq.com
QQ group:108895031

## License
```
Copyright 2015 HaoGe

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
