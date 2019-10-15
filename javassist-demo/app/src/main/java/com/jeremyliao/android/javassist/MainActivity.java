package com.jeremyliao.android.javassist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PrintLogDemo demo = new PrintLogDemo();
        demo.getBoolValue();
        demo.getIntValue();
        demo.parseLong(100);
        demo.testLog();
    }
}
