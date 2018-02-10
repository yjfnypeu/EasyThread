package com.lzh.easythreadmanager.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lzh.easythread.AsyncCallback;
import com.lzh.easythread.EasyThread;
import com.lzh.easythread.Callback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    EasyThread executor = null;

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.thread_name);
        executor = EasyThread.Builder
                .fixed(2)
                .priority(Thread.MAX_PRIORITY)
                .name("default thread name")
                .build();
    }

    public void onNormalClick (View v) {
        resetThreadName();
        executor.callback(new ThreadCallback())
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("MainActivity", "在子线程处理一些任务");
                    }
                });

        AsyncCallback<User> async = new AsyncCallback<User>() {
            @Override
            public void onSuccess(User user) {
                System.out.println("user = [" + user + "]");
            }

            @Override
            public void onFailed(Throwable t) {
                System.out.println("t = [" + t + "]");
            }
        };

        executor.name("test submit")
                .callback(new ThreadCallback())
                // 使用异步任务
                .async(new Callable<User>() {
                    @Override
                    public User call() throws Exception {
                        User user = new User();
                        user.username = "豪哥";
                        user.password = "123456";
                        return user;
                    }
                }, async);
    }

    public void onExceptionClick (View v) {
        resetThreadName();
        executor.callback(new ThreadCallback())
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException("故意在子线程抛出异常");
                    }
                });
    }

    private class ThreadCallback implements Callback {

        @Override
        public void onError(Thread thread, Throwable t) {
            Toast.makeText(MainActivity.this, String.format("线程%s运行出现异常，异常信息为：%s", thread, t.getMessage()),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCompleted(Thread thread) {
            Toast.makeText(MainActivity.this, String.format("线程%s运行完毕", thread),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart(Thread thread) {

        }
    }

    private void resetThreadName() {
        String name = editText.getText().toString();
        if (!TextUtils.isEmpty(name)) {
            executor.name(name);
        }
    }
}
