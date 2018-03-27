# EasyThread ![travis-ci](https://travis-ci.org/yjfnypeu/EasyThread.svg?branch=master)
<a href="http://www.methodscount.com/?lib=com.github.yjfnypeu%3AEasyThread%3A0.1"><img src="https://img.shields.io/badge/Methods count-61-e91e63.svg"/></a>

EasyThread通过对原生的线程池进行封装，可让你更方便的进行线程任务操作。

## 特性

- **简单轻巧**:方法数不过百，无额外次级依赖。
- **配置灵活**:可方便、灵活的对每次所启动的任务，配置线程名、线程优先级等。
- **使用安全**:当线程出现异常。能自动将catch异常信息传递给用户，避免出现crash。
- **线程切换**:自带线程切换功能：指定任务执行后，在哪个线程中进行用户通知。
- **回调通知**:当任务启动时与任务运行完毕后。有分别的生命周期作为通知。
- **任务扩展**:支持*延迟任务*以及*异步回调任务*

## 依赖

lastestVersion = [![](https://jitpack.io/v/yjfnypeu/EasyThread.svg)](https://jitpack.io/#yjfnypeu/EasyThread)

```groovy
// 添加jitPack仓库使用
maven { url 'https://jitpack.io' }

// 添加依赖
compile "com.github.yjfnypeu:EasyThread:$lastestVersion"
```

## 基本用法

使用方式分两步走：

- 第一步：创建EasyThread实例。每个EasyThread实例会持有一个独立的线程池提供使用。

```
EasyThread easyThread = EasyThread.Builder
            //提供了四种create方法，用于根据需要创建不同类型的线程池进行使用
            //比如createSingle():表示创建一个单例的线程池进行使用
            .createXXX()
            .build();
```

- 第二步：使用创建的EasyThread实例进行任务执行：

EasyThread支持执行四种任务：

### 1. 普通Runnable任务

```
easyThread.execute(new Runnable(){
    @Override
    public void run() {
        // do something.
    }
});
```

### 2. 普通Callable任务

```
Future task = easyThread.submit(new Callback<User>(){
    @Override
    public User call() throws Exception {
        // do something
        return user;
    }
})
User result = task.get();
```
### 3. 异步回调任务

```
// 异步执行任务
Callable<User> callable = new Callable<User>(){
    @Override
    public User call() throws Exception {
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

### 4. 延迟后台任务

```
// 在启动任务前，调用delay方法，指定延迟时间即可
easyThread.setDelay(time, unit)
    .execute(runnable);
```

**e.g 延迟3秒启动执行任务**

```
easyThread.setDelay(3, TimeUnit.SECONDS)
        .execute(task);
```

## 高级配置

EasyThread提供了各种的额外配置，通过这些配置可以让线程操作使用起来更加得心应手。

### 两种配置方式

这里我们以配置后台任务名进行说明：

**1. 配置默认线程任务名(默认配置)**

```
EasyThread.Builder.createXXX().setName(name);
```

**2. 配置当前线程任务名(当前任务配置)**

```
easyThread.setName(name).execute(task);
```

### 线程优先级及线程名

配置方式：

```
easyThread.setName(name)// 配置线程任务名
	.setPriority()// 配置线程运行优先级
```

### 任务回调通知

接口说明：

```java
public interface Callback {
    // 线程任务启动时的通知
    void onStart (String threadName);
    // 线程任务运行时出现异常时的通知
    void onError (String threadName, Throwable t);
    // 线程任务正常执行完成时的通知
    void onCompleted (String threadName);
}
```

配置方式：

```
easyThread.setCallback(callback);
```

### 消息派发器

消息派发器用于消息回调线程切换，即指定回调任务需要运行在什么线程之上。

比如说在Android平台，很常见的就是回调时需要进行界面通知，所以这个时候就需要回调通知运行在UI线，便于操作。

配置方式：

```
// 派发器的实例类型为java.util.concurrent.Executor子类
easyThread.setDeliver(deliver);
```

在默认条件下(即未配置额外的派发器时)，在Android或者Java平台，分别适配了不同的回调派发逻辑：

- **在纯java环境下：回调方法所运行的线程与任务执行线程一致**
- **在Android环境：回调方法默认运行于主线程**

## 推荐配置

对于APP来说。线程资源是宝贵的。为了避免创建过多额外的线程，所以建议对每个app。提供一个统一的管理器维护所有的线程池，如下所示：

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
        io = EasyThread.Builder.createFixed(6).setName("IO").setPriority(7).setCallback(new DefaultCallback()).build();
        cache = EasyThread.Builder.createCacheable().setName("cache").setCallback(new DefaultCallback()).build();
        calculator = EasyThread.Builder.createFixed(4).setName("calculator").setPriority(Thread.MAX_PRIORITY).setCallback(new DefaultCallback()).build();
        file = EasyThread.Builder.createFixed(4).setName("file").setPriority(3).setCallback(new DefaultCallback()).build();
    }

    private static class DefaultCallback implements Callback {

        @Override
        public void onError(String threadName, Throwable t) {
            MyLog.e("Task with thread %s has occurs an error: %s", threadName, t.getMessage());
        }

        @Override
        public void onCompleted(String threadName) {
            MyLog.d("Task with thread %s completed", threadName);
        }

        @Override
        public void onStart(String threadName) {
            MyLog.d("Task with thread %s start running!", threadName);
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
