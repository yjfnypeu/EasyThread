package com.lzh.easythreadmanager.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.lzh.easythread.EasyThread;
import com.lzh.easythread.ErrorCallback;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick (View v) {
        EasyThread executor = EasyThread.Builder.cacheable()
                .name("Test Cacheable")
                .callback(new ErrorCallback() {
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this, "线程出异常了！：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .priority(Thread.MAX_PRIORITY)
                .build();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("故意的");
            }
        });
    }
}
