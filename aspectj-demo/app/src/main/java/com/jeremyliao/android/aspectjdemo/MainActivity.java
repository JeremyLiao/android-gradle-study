package com.jeremyliao.android.aspectjdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jeremyliao.android.apilib.PrintLogDemo;
import com.jeremyliao.android.apilib.TestTarget;
import com.jeremyliao.android.base.annotation.ExecutionTime;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();
        TestTarget target = new TestTarget();
        target.getBoolValue();
        target.getIntValue();
        target.parseLong(100);
        PrintLogDemo demo = new PrintLogDemo();
        demo.getBoolValue();
        demo.getIntValue();
        demo.parseLong(100);
    }

    @ExecutionTime
    private void test() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
