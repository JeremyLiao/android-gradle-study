package com.jeremyliao.android.asm;

/**
 * Created by liaohailiang on 2019-09-26.
 */
public class TestTarget {

    public int getIntValue() {
        return 10;
    }

    public boolean getBoolValue() {
        return true;
    }

    public int parseLong(long value) {
        return (int) value;
    }
}
