package com.lzh.easythreadmanager.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lzh.easythread.EasyThread;
import com.lzh.easythread.Callback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends Activity {

    EasyThread executor = null;

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.thread_name);
        executor = EasyThread.Builder.fixed(2)
                .priority(Thread.MAX_PRIORITY)
                .name("测试线程")
                .build();
    }

    public void onNormalClick (View v) {
        executor.name(editText.getText().toString())
                .callback(new ThreadCallback())
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("MainActivity", "在子线程处理一些任务");
                    }
                });

        Future<User> submit = executor.name("test submit")
                .callback(new ThreadCallback())
                .submit(new Callable<User>() {
                    @Override
                    public User call() throws Exception {
                        User user = new User();
                        user.username = "豪哥";
                        user.password = "123456";
                        return user;
                    }
                });
        try {
            User user = submit.get();
            System.out.println(user);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void onExceptionClick (View v) {
        executor.name(editText.getText().toString())
                .callback(new ThreadCallback())
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
    }
}
